// vars/buildUtils.groovy

// Run a map of tasks with maxParallel at once
def runWithMaxParallel(tasks, maxParallel = 3) {
    def keys = tasks.keySet() as List
    def total = keys.size()

    for (int i = 0; i < total; i += maxParallel) {
        // 🔑 convert subList to real List so it's serializable
        def slice = new ArrayList(keys.subList(i, Math.min(i + maxParallel, total)))
        def batch = [:]
        slice.each { k -> batch[k] = tasks[k] }
        parallel batch
    }
}


def build(repo, envConf) {
    if (repo.buildType == "nextjs") {
        buildNextjs(repo, envConf)
    // } else if (repo.buildType == "docker") {
    //     buildDocker(repo, envConf, idx)
    } else {
        echo "❌ Unknown repo.buildType '${repo.buildType}' for ${repo.name}"
    }
}


def setupBuild(repo){
    if (repo.buildType == "nextjs") {
        installNextjs(repo)
    // } else if (repo.buildType == "docker") {
    //     buildDocker(repo, envConf, idx)
    } else {
        echo "❌ Unknown repo.buildType '${repo.buildType}' for ${repo.name}"
    }
}

private def installNextjs(repo){
    dir(repo.folder) {
        def workspaceDir = pwd()  // absolute path to current dir (safe in Jenkins)
        echo "=== Building ${repo.folder} branch >>${repo.branch}<< ==="

        // Check if package.json exists in this folder
        if (!fileExists("package.json")) {
            echo "⏭️ Skipping build for ${repo.folder}, no package.json found."
            return
        }

        sh '''
            rm -rf shared_modules
            mkdir -p shared_modules
            cp package.json package-lock.json shared_modules/
            export CI=true
            npm ci --prefix shared_modules
            rm -rf .next/cache .next/server || true
            rm -rf .next/**/*.nft.json || true
            rm -rf buildEnvs
        '''


        
        sh """
            mkdir -p ${workspaceDir}/outs
        """
    }
}

private def buildNextjs(repo, envConf) {
    def configBasePath = "${env.WORKSPACE}/site_configs"

    dir(repo.folder) {
        def workspaceDir = pwd()  // absolute path to current dir (safe in Jenkins)

        def envName   = envConf.name
        def domain    = commonUtils.extractDomain(envConf.MAIN_DOMAIN)
        def envOut    = "${workspaceDir}/outs/${envName}"
        def buildPath = "${workspaceDir}/buildEnvs/${envName}"
        echo "=== Building ${repo.folder} branch >>${repo.branch}<< for env ${envName} ==="

        // Check if package.json exists in this folder
        if (!fileExists("package.json")) {
            echo "⏭️ Skipping build for ${repo.folder}, no package.json found."
            return
        }


        
        if (domain && redisState.isMissingCert(domain)) {
            echo "⏭️ Skipping ${envName}, missing cert for ${domain}"
            return
        }

        sh """
            mkdir -p ${buildPath}
            rsync -a  --exclude=node_modules \
                --exclude=buildEnvs \
                --exclude=.git \
                --exclude=.next \
                ./ ${buildPath}

            ln -s ${workspaceDir}/shared_modules/node_modules ${buildPath}/node_modules
        """

        dir(buildPath) {
            withEnv(envConf.collect { k,v -> "${k.toUpperCase()}=${v}" }) {
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
                        echo "✅ Build output exists for ${repo.folder}/${envName}"
                    else
                        echo "❌ ERROR: ${envOut} missing or empty for ${repo.folder}"
                        exit 1
                    fi
                """
            }
        }
    }
}
            
            // echo "📦 Building Next.js project: ${repo.folder} for env ${envConf.name}"

            // def domain = commonUtils.extractDomain(envConf.MAIN_DOMAIN)

            // if (!(domain && redisState.isMissingCert(domain))) {
            
            //     def envOut   = "${workspaceDir}/outs/${envConf.name}"
            //     def buildPath = "${workspaceDir}/buildEnvs/${envConf.name}"

            //     sh """
            //         mkdir -p ${buildPath}
            //         rsync -a --exclude=node_modules ./ ${buildPath}
            //         ln -s ${workspaceDir}/shared_modules/node_modules ${buildPath}/node_modules
            //     """

            //     dir(buildPath){
            //         withEnv(envConf.collect { k,v -> "${k.toUpperCase()}=${v}" } ) {
            //             sh '''
            //                 if [ -f package.json ]; then
            //                     npx next build && npx next-sitemap
            //                     rm -rf .next/cache .next/server || true
            //                     rm -rf .next/**/*.nft.json || true
            //                 else
            //                     echo "No package.json found, skipping build."
            //                 fi
            //             '''

            //             sh """
            //                 rm -rf ${envOut} || true
            //                 cp -r out ${envOut} || echo "⚠️ Warning: 'out' folder missing, copy skipped"
            //             """

            //             sh """
            //                 if [ -d ${envOut} ] && [ "\$(ls -A ${envOut})" ]; then
            //                     echo "✅ Build output exists for ${repo.folder}/${envConf.name}"
            //                 else
            //                     echo "❌ ERROR: ${envOut} missing or empty for ${repo.folder}"
            //                     exit 1
            //                 fi
            //             """            
            //         }
            //     } 
            // } else {
            //     echo "⏭️ Skipping ${envConf.name}, missing cert for ${domain}"
            // }


// Only export `build` helper
return [ build: this.&build, setupBuild: this.&setupBuild ]
