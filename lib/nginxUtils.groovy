def generate(repo, envConf, vpsInfo, nginxTemplate, missingCerts) {

    def domain = commonUtils.extractDomain(envConf.MAIN_DOMAIN)

    // skip if cert missing
    if (missingCerts.contains(domain.toLowerCase())) {
        echo "⏭️ Skipping nginx config for ${envConf.name} (${domain}), cert missing"
        return
    }

    if (repo.type == "nextjs") {
        generateNextjs(repo, envConf, vpsInfo, domain, nginxTemplate)
    } else if (repo.type == "docker") {
        generateDocker(repo, envConf, vpsInfo, domain, nginxTemplate)
    } else {
        error "❌ Unknown repo.type '${repo.type}' for nginx config"
    }
}

private def generateNextjs(repo, envConf, vpsInfo, domain, nginxTemplate) {
    def tmpConfigFile = "${envConf.name}.conf"
    def nginxConfig = nginxTemplate
        .replace('{{DOMAIN}}', domain)
        .replace('{{ENV_NAME}}', envConf.name)
        .replace('{{WEBROOT_BASE}}', vpsInfo.webrootBase)

    writeFile(file: tmpConfigFile, text: nginxConfig)
    echo "✅ Generated Nginx config for Next.js ${envConf.name}: ${tmpConfigFile}"

    sshagent(credentials: [vpsInfo.vpsCredId]) {
        sh """
            scp -o StrictHostKeyChecking=no ${tmpConfigFile} ${vpsInfo.vpsUser}@${vpsInfo.vpsHost}:/home/${vpsInfo.vpsUser}/${tmpConfigFile}
            ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} "
                sudo mv /home/${vpsInfo.vpsUser}/${tmpConfigFile} /etc/nginx/sites-available/${tmpConfigFile} &&
                sudo ln -sf /etc/nginx/sites-available/${tmpConfigFile} /etc/nginx/sites-enabled/${tmpConfigFile}
            "
        """
    }
}

private def generateDocker(repo, envConf, vpsInfo, domain, nginxTemplate) {
    def tmpConfigFile = "${envConf.name}.conf"
    def nginxConfig = nginxTemplate
        .replace('{{DOMAIN}}', domain)
        .replace('{{ENV_NAME}}', envConf.name)
        .replace('{{WEBROOT_BASE}}', vpsInfo.webrootBase)

    writeFile(file: tmpConfigFile, text: nginxConfig)
    echo "✅ Generated Nginx config for Docker ${envConf.name}: ${tmpConfigFile}"

    sshagent(credentials: [vpsInfo.vpsCredId]) {
        sh """
            scp -o StrictHostKeyChecking=no ${tmpConfigFile} ${vpsInfo.vpsUser}@${vpsInfo.vpsHost}:/home/${vpsInfo.vpsUser}/${tmpConfigFile}
            ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} "
                sudo mv /home/${vpsInfo.vpsUser}/${tmpConfigFile} /etc/nginx/sites-available/${tmpConfigFile} &&
                sudo ln -sf /etc/nginx/sites-available/${tmpConfigFile} /etc/nginx/sites-enabled/${tmpConfigFile}
            "
        """
    }
}
