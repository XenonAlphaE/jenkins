def generate(repo, envConf, vpsInfos) {
    if (repo.buildType == "nextjs") {
        generateNextjs(repo, envConf, vpsInfos)
    }else if (repo.buildType == "proxy") {
        generateProxy(repo, envConf, vpsInfos)
    } else {
        error "❌ Unknown repo.type '${repo.type}' for nginx config"
    }
}

private def generateNextjs(repo, envConf, vpsInfos) {
    def nginxTemplate = readFile('nginx/https.template.conf')
    if (repo.ssl?.toLowerCase() == 'cloudflare') {
        nginxTemplate = readFile('nginx/https.cloudflare.conf')
    }
     
    def vpsInfo = vpsInfos[repo.vpsRef]
    dir(repo.folder) {
        def domain = commonUtils.extractDomain(envConf.MAIN_DOMAIN)

        if (redisState.isMissingCert(domain)) {
            echo "⏭️ Skipping nginx config for ${envConf.name} (${domain}) due to missing cert"
            return
        }

        def tmpConfigFile = "${envConf.name}.conf"
        def nginxConfig = nginxTemplate
            .replace('{{DOMAIN}}', domain)
            .replace('{{ENV_NAME}}', envConf.name)
            .replace('{{WEBROOT_BASE}}', vpsInfo.webrootBase)

        writeFile(file: tmpConfigFile, text: nginxConfig)
        echo "✅ Generated Nginx config for ${envConf.name} locally: ${tmpConfigFile}"
        echo "📄 Local nginx config content for ${envConf.name}:\n${nginxConfig}"

        sshagent(credentials: [vpsInfo.vpsCredId]) {
            sh """
                # Copy config to VPS
                scp -o StrictHostKeyChecking=no ${tmpConfigFile} ${vpsInfo.vpsUser}@${vpsInfo.vpsHost}:/home/${vpsInfo.vpsUser}/${tmpConfigFile}

                # SSH into VPS and deploy
                ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} "

                    # 👉 Remove all conflicting enabled sites for this domain
                    for f in /etc/nginx/sites-enabled/*; do
                        if grep -qE \\"server_name .*(${domain}).*;\\" \"\$f\"; then
                            echo 'Removing conflicting site: \$f'
                        fi
                    done

                    sudo mv /home/${vpsInfo.vpsUser}/${tmpConfigFile} /etc/nginx/sites-available/${tmpConfigFile} &&
                    sudo chown root:root /etc/nginx/sites-available/${tmpConfigFile} &&


                    # 👉 activate only this site
                    sudo ln -sf /etc/nginx/sites-available/${tmpConfigFile} /etc/nginx/sites-enabled/${tmpConfigFile} 
                "

                # Optional: view deployed config
                ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} "cat /etc/nginx/sites-available/${tmpConfigFile}"
            """
        }

        
    }

}


private def generateProxy(repo, envConf, vpsInfos) {
    def nginxTemplate = readFile('nginx/https.proxy.conf')
     
    def vpsInfo = vpsInfos[repo.vpsRef]
    dir(repo.folder) {
        def domain = commonUtils.extractDomain(envConf.MAIN_DOMAIN)

        if (redisState.isMissingCert(domain)) {
            echo "⏭️ Skipping nginx config for ${envConf.name} (${domain}) due to missing cert"
            return
        }

        def tmpConfigFile = "${envConf.name}.conf"
        def nginxConfig = nginxTemplate
            .replace('{{DOMAIN}}', domain)
            .replace('{{PROXY_PORT}}', repo.proxyPort)

        writeFile(file: tmpConfigFile, text: nginxConfig)
        echo "✅ Generated Nginx config for ${envConf.name} locally: ${tmpConfigFile}"
        echo "📄 Local nginx config content for ${envConf.name}:\n${nginxConfig}"

        sshagent(credentials: [vpsInfo.vpsCredId]) {
            sh """
                # Copy config to VPS
                scp -o StrictHostKeyChecking=no ${tmpConfigFile} ${vpsInfo.vpsUser}@${vpsInfo.vpsHost}:/home/${vpsInfo.vpsUser}/${tmpConfigFile}

                # SSH into VPS and deploy
                ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} "

                    # 👉 Remove all conflicting enabled sites for this domain
                    for f in /etc/nginx/sites-enabled/*; do
                        if grep -qE \\"server_name .*(${domain}).*;\\" \"\$f\"; then
                            echo 'Removing conflicting site: \$f'
                        fi
                    done

                    sudo mv /home/${vpsInfo.vpsUser}/${tmpConfigFile} /etc/nginx/sites-available/${tmpConfigFile} &&
                    sudo chown root:root /etc/nginx/sites-available/${tmpConfigFile} &&


                    # 👉 activate only this site
                    sudo ln -sf /etc/nginx/sites-available/${tmpConfigFile} /etc/nginx/sites-enabled/${tmpConfigFile} 
                "

                # Optional: view deployed config
                ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} "cat /etc/nginx/sites-available/${tmpConfigFile}"
            """
        }

        
    }

}


return [ generate: this.&generate ]
