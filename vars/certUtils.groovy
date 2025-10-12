def generateCert(domainInfo, envConf, vpsInfos) {
    if (domainInfo.ssl == "cloudfare") {
        copyCertFromCloudfare(domainInfo, envConf, vpsInfos)
    } else {
        genrateCertbot(domainInfo, envConf, vpsInfos)
    }
}

//
// 🔹 Copy existing Cloudflare-origin certificates into VPS
//
private def copyCertFromCloudfare(domainInfo, envConf, vpsInfos) {
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

        // Upload cert & key (assumes files exist in Jenkins workspace or envConf)
        sh """
            scp -o StrictHostKeyChecking=no ${certBasePath}/${domain}/${domain}.crt ${vpsInfo.vpsUser}@${vpsInfo.vpsHost}:/home/${vpsInfo.vpsUser}/ &&
            scp -o StrictHostKeyChecking=no ${certBasePath}/${domain}/${domain}.key ${vpsInfo.vpsUser}@${vpsInfo.vpsHost}:/home/${vpsInfo.vpsUser}/ &&
            ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} '
                sudo mv /home/${vpsInfo.vpsUser}/${domain}.crt /etc/ssl/cloudflare/${domain}/${domain}.crt &&
                sudo mv /home/${vpsInfo.vpsUser}/${domain}.key /etc/ssl/cloudflare/${domain}/${domain}.key &&
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

        def nginxConfig = envConf.certbotTemplate
            .replace('{{DOMAIN}}', domain)
            .replace('{{ENV_NAME}}', domainInfo.name)
            .replace('{{WEBROOT_BASE}}', vpsInfo.webrootBase)

        writeFile(file: tmpConfigFile, text: nginxConfig)
        echo "✅ Generated Nginx config for ${domainInfo.name}: ${tmpConfigFile}"

        sh """
            scp -o StrictHostKeyChecking=no ${tmpConfigFile} ${vpsInfo.vpsUser}@${vpsInfo.vpsHost}:/home/${vpsInfo.vpsUser}/${tmpConfigFile}
            ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} "
                sudo mv /home/${vpsInfo.vpsUser}/${tmpConfigFile} /etc/nginx/sites-available/${tmpConfigFile} &&
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
