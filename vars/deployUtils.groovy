def deploy(repo, envConf, vpsInfos) {
    if (repo.type == "nextjs") {
        deployNextjs(repo, envConf, vpsInfos)
    // } else if (repo.type == "docker") {
    //     deployDocker(repo, envConf, vpsInfo)
    } else {
        error "❌ Unknown repo.type '${repo.type}' for ${repo.folder}"
    }
}

private def deployNextjs(repo, envConf, vpsInfos) {
    def vpsInfo = vpsInfos[repo.vpsRef]
    dir(repo.folder) {
        def domain = extractDomain(envConf.MAIN_DOMAIN)

        if (state().hasMissingCert(domain)) {
            echo "⏭️ Skipping deploy for ${envConf.name} (${domain}) due to missing cert"
            return
        }

        def envOut = "outs/${envConf.name}"
        echo "🚀 Deploying ${envOut} to ${vpsInfo.vpsHost}:${vpsInfo.webrootBase}/${envConf.name}"

        sshagent (credentials: [vpsInfo.vpsCredId]) {
            sh """
                tar -czf ${envConf.name}.tar.gz -C outs/${envConf.name} .
                scp -o StrictHostKeyChecking=no ${envConf.name}.tar.gz ${vpsInfo.vpsUser}@${vpsInfo.vpsHost}:/tmp/

                ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} "
                    sudo mkdir -p ${vpsInfo.webrootBase}/${envConf.name} &&
                    sudo tar -xzf /tmp/${envConf.name}.tar.gz -C ${vpsInfo.webrootBase}/${envConf.name} &&
                    rm /tmp/${envConf.name}.tar.gz &&
                    sudo chown -R www-data:www-data ${vpsInfo.webrootBase}/${envConf.name}
                "
            """
        }
        
    }
}

private def deployDocker(repo, envConf, vpsInfo) {
    echo "🚀 Deploying Docker ${repo.folder}/${envConf.name} to ${vpsInfo.vpsHost}"

    sshagent (credentials: [vpsInfo.vpsCredId]) {
        sh """
            ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} "
                docker stop ${repo.folder}-${envConf.name} || true &&
                docker rm ${repo.folder}-${envConf.name} || true &&
                docker run -d --name ${repo.folder}-${envConf.name} -p 80:80 ${repo.folder}:${envConf.name}
            "
        """
    }
}

// Only export `deploy` helper
return [ deploy: this.&deploy ]
