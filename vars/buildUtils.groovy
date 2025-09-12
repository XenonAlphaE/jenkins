// vars/buildUtils.groovy

def build(repo) {
    if (repo.buildType == "nextjs") {
        buildNextjs(repo)
    // } else if (repo.buildType == "docker") {
    //     buildDocker(repo, envConf, idx)
    } else {
        error "❌ Unknown repo.buildType '${repo.buildType}' for ${repo.name}"
    }
}


private def buildNextjs(repo) {
    dir(repo.folder) {
        echo "📦 Building Next.js project: ${repo.folder} for env ${envConf.name}"
        def workspaceDir = pwd()  // absolute path to current dir (safe in Jenkins)

        echo "=== Building ${repo.folder} branch >>${repo.branch}<< ==="

        // Check if package.json exists in this folder
        if (!fileExists("package.json")) {
            echo "⏭️ Skipping build for ${repo.folder}, no package.json found."
            return
        }

        sh '''
            export CI=true
            npm ci --prefix shared_modules
            rm -rf .next/cache .next/server || true
            rm -rf .next/**/*.nft.json || true
            rm -rf buildEnvs
        '''
        
        sh """
            mkdir -p ${workspaceDir}/outs
        """

        repo.envs.eachWithIndex { envConf, idx ->
            def domain = commonUtils.extractDomain(envConf.MAIN_DOMAIN)

            if (!(domain && redisState.isMissingCert(domain))) {
            
                def envOut   = "${workspaceDir}/outs/${envConf.name}"
                def buildPath = "${workspaceDir}/buildEnvs/${envConf.name}"

                sh """
                    mkdir -p ${buildPath}
                    rsync -a --exclude=node_modules ./ ${buildPath}
                    ln -s ${workspaceDir}/shared_modules/node_modules ${buildPath}/node_modules
                """

                dir(buildPath){
                    withEnv(envConf.collect { k,v -> "${k.toUpperCase()}=${v}" } ) {
                        sh '''
                            if [ -f package.json ]; then
                                npx next build && npx next-sitemap
                                rm -rf .next/cache .next/server || true
                                rm -rf .next/**/*.nft.json || true
                            else
                                echo "No package.json found, skipping build."
                            fi
                        '''

                        sh """
                            rm -rf ${envOut} || true
                            cp -r out ${envOut} || echo "⚠️ Warning: 'out' folder missing, copy skipped"
                        """

                        sh """
                            if [ -d ${envOut} ] && [ "\$(ls -A ${envOut})" ]; then
                                echo "✅ Build output exists for ${repo.folder}/${envConf.name}"
                            else
                                echo "❌ ERROR: ${envOut} missing or empty for ${repo.folder}"
                                exit 1
                            fi
                        """            
                    }
            } else {
                echo "⏭️ Skipping ${envConf.name}, missing cert for ${domain}"
            }

        }
    }
}

// Only export `build` helper
return [ build: this.&build ]
