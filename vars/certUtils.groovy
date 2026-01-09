def generateCert(domainInfo, vpsInfos) {
    if (domainInfo.ssl == "cloudflare") {
        copyCertFromCloudflare(domainInfo, vpsInfos)
    } else {
        genrateCertbot(domainInfo, vpsInfos)
    }
}

//
// 🔹 Copy existing Cloudflare-origin certificates into VPS
//
private def copyCertFromCloudflare(domainInfo, vpsInfos) {
    def vpsInfo = vpsInfos[domainInfo.vpsRef]
    def domain = normalizeDomain(domainInfo.MAIN_DOMAIN)
    def certBasePath = "${env.WORKSPACE}/claufare_ssl"

    echo "📦 Copying Cloudflare certificate for ${domain} to ${vpsInfo.vpsHost}"

    sshagent (credentials: [vpsInfo.vpsCredId]) {
        sh """
            ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} '
                sudo mkdir -p /etc/ssl/cloudflare/${domain} &&
                sudo chown -R root:root /etc/ssl/cloudflare/${domain}
            '
        """

        sh """
            scp -o StrictHostKeyChecking=no ${certBasePath}/${domain}/${domain}.crt ${vpsInfo.vpsUser}@${vpsInfo.vpsHost}:/tmp/ &&
            scp -o StrictHostKeyChecking=no ${certBasePath}/${domain}/${domain}.key ${vpsInfo.vpsUser}@${vpsInfo.vpsHost}:/tmp/ &&
            ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} '
                sudo mv /tmp/${domain}.crt /etc/ssl/cloudflare/${domain}/${domain}.crt &&
                sudo mv /tmp/${domain}.key /etc/ssl/cloudflare/${domain}/${domain}.key &&
                sudo chmod 600 /etc/ssl/cloudflare/${domain}/${domain}.key &&
                sudo chmod 600 /etc/ssl/cloudflare/${domain}/${domain}.crt
            '
        """

        echo "✅ Cloudflare cert deployed for ${domain}"
    }
}

//
// 🔹 Generate Let’s Encrypt (Certbot) certificates dynamically
//
private def genrateCertbot(domainInfo, vpsInfos) {
    def vpsInfo = vpsInfos[domainInfo.vpsRef]
    def domain = normalizeDomain(domainInfo.MAIN_DOMAIN)
    def certbotTemplate = readFile('nginx/http.template.conf')

    sshagent (credentials: [vpsInfo.vpsCredId]) {

        def exists = sh(
            script: """
                ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} \
                "sudo test -f /etc/letsencrypt/live/${domain}/fullchain.pem && echo yes || echo no"
            """,
            returnStdout: true
        ).trim()

        if (exists == "yes") {
            echo "🔑 Certificate already exists for ${domain} on ${vpsInfo.vpsHost}"
            return
        }

        echo "❌ No certificate for ${domain}, issuing new one"

        def domainIp = sh(script: "dig +short ${domain} | tail -n1", returnStdout: true).trim()
        if (!domainIp) {
            echo "⚠️ Cannot resolve domain ${domain}, skipping cert issuance."
            return
        }

        echo "🔍 Domain ${domain} resolves to ${domainIp}, VPS expected IP is ${vpsInfo.vpsHost}"

        if (domainIp != vpsInfo.vpsHost) {
            echo "❌ Domain ${domain} does not point to expected VPS ${vpsInfo.vpsHost}, skipping cert issuance."
            return
        }

        def tmpConfigFile = "${domainInfo.name}.conf"
        def nginxConfig = certbotTemplate
            .replace('{{DOMAIN}}', domain)
            .replace('{{ENV_NAME}}', domainInfo.name)
            .replace('{{WEBROOT_BASE}}', vpsInfo.webrootBase)


        writeFile(file: tmpConfigFile, text: nginxConfig)
        echo "✅ Generated Nginx config for ${domainInfo.name}: ${tmpConfigFile}"

        sh """
            scp -o StrictHostKeyChecking=no ${tmpConfigFile} ${vpsInfo.vpsUser}@${vpsInfo.vpsHost}:/tmp/${tmpConfigFile}
            ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} "
                cat /tmp/${tmpConfigFile}
            "
            ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} "
                sudo mv /tmp/${tmpConfigFile} /etc/nginx/sites-available/${tmpConfigFile} &&
                sudo ln -sf /etc/nginx/sites-available/${tmpConfigFile} /etc/nginx/sites-enabled/${tmpConfigFile} &&
                sudo nginx -t &&
                sudo systemctl reload nginx
            "
        """

        sh """
            ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} \\
            "sudo mkdir -p ${vpsInfo.webrootBase}/${domainInfo.name}/.well-known/acme-challenge && \\
                sudo chown -R www-data:www-data ${vpsInfo.webrootBase}/${domainInfo.name} && \\
                sudo certbot certonly --webroot -w ${vpsInfo.webrootBase}/${domainInfo.name} \\
                -d ${domain} -d www.${domain} \\
                -v --agree-tos --email contact@${domain} --non-interactive"
        """
    }
}

//
// 🔹 Helper: Normalize domain names (remove protocol, slashes, www)
//
private def normalizeDomain(String domain) {
    return domain
        .replaceAll('https://','')
        .replaceAll('http://','')
        .replaceAll('/','')
        .replaceAll('^www\\.', '')
}
