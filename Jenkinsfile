// --- Helper functions ---
def extractDomain(String url) {
    return url
        .replaceAll(/^https?:\/\//, '')  // remove http(s)
        .replaceAll(/\/$/, '')           // remove trailing slash
        .replaceAll(/^www\./, '')        // strip leading www
}

// Check if domain exists in MISSING_CERTS string
def isMissingCert(String domain, String missingCertsStr) {
    if (!missingCertsStr?.trim()) {
        return false
    }
    def domains = missingCertsStr.split(',').collect { it.trim() }
    return domains.contains(domain)
}
// Check if new comit
def isNewCommit(String repo, String newChangesRepo) {
    if (!newChangesRepo?.trim()) {
        return false
    }
    def repoList = newChangesRepo.split(',').collect { it.trim() }
    return repoList.contains(repo)
}

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

pipeline {
    agent any
    tools {
        nodejs 'NODE_20'
    }

    parameters {
        booleanParam(
            name: 'FORCE_BUILD_ALL',
            defaultValue: false,
            description: 'Force build & deploy all repos, even if no new changes'
        )
    }

    environment {
        // store missing domains across stages
        MISSING_CERTS = ""
        CHANGED_REPOS = ""
    }

    stages {
        stage('Load Script') {
            steps {
                script {
                    repos = load 'repos.groovy'
                    vpsInfos = load 'vps.groovy'
                    ngnixTemplate = readFile('ngnix/https.template.conf')
                }
            }
        }

        stage('Check Certificates') {
            steps {
                script {
                    def missing = []

                    repos.each { repo ->
                        def vpsInfo = vpsInfos[repo.vpsRef]
                        repo.envs.each { site ->
                            def domain = extractDomain(site.MAIN_DOMAIN)

                            sshagent (credentials: [vpsInfo.vpsCredId]) {
                                def exists = sh(
                                    script: """
                                        ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} \
                                        "sudo test -f /etc/letsencrypt/live/${domain}/fullchain.pem && echo yes || echo no"
                                    """,
                                    returnStdout: true
                                ).trim()

                                if (exists == "no") {
                                    echo "⚠️  Certificate missing for ${domain}"
                                    missing << domain
                                } else {
                                    echo "✅ Certificate exists for ${domain}"
                                }
                            }
                        }
                    }

                    if (missing) {
                        echo "⚠️  Some certificates are missing: ${missing.join(', ')}"
                        env.MISSING_CERTS = missing.join(',')
                    } else {
                        echo "✅ All certificates present"
                    }
                }
            }
        }

        stage('Repos Pulls') {
            steps {
                script {
                    def parallelTasks = [:]


                    repos.each { repo ->
                        parallelTasks["Pull-${repo.folder}"] = {
                            dir(repo.folder) {
                                def vpsInfo = vpsInfos[repo.vpsRef]
                                def changed = false

                                if (!fileExists('.git')) {
                                    // First time clone
                                    checkout([
                                        $class: 'GitSCM',
                                        branches: [[name: "*/${repo.branch}"]],
                                        doGenerateSubmoduleConfigurations: false,
                                        userRemoteConfigs: [[
                                            url: repo.url,
                                            credentialsId: repo.credId
                                        ]],
                                        extensions: [
                                            // Optimize repo checkout
                                            [$class: 'CloneOption', depth: 1, noTags: true, shallow: true],
                                            [$class: 'PruneStaleBranch']
                                        ]
                                    ])
                                    changed = true  
                                } else {
                                    def oldCommit = sh(script: "git rev-parse HEAD", returnStdout: true).trim()

                                    checkout([
                                        $class: 'GitSCM',
                                        branches: [[name: "*/${repo.branch}"]],
                                        doGenerateSubmoduleConfigurations: false,
                                        userRemoteConfigs: [[
                                            url: repo.url,
                                            credentialsId: repo.credId
                                        ]],
                                        extensions: [
                                            [$class: 'PruneStaleBranch'],
                                            [$class: 'CloneOption', depth: 1, noTags: true, shallow: true]
                                        ]
                                    ])

                                    def newCommit = sh(script: "git rev-parse HEAD", returnStdout: true).trim()

                                    if (oldCommit != newCommit) {
                                        echo "🔄 Changes detected in ${repo.folder}: ${oldCommit} → ${newCommit}"
                                        changed = true
                                    } else {
                                        echo "⏭️ No changes in ${repo.folder}"
                                        changed = false

                                    }
                                }

                                // 👇 write result to a file for later collection
                                writeFile file: "${repo.folder}.changed", text: changed.toString()
                            }
                        }
                    }

                    runWithMaxParallel(parallelTasks, 3)  // 👈 cap parallelism
                    // Merge after parallel by reading files
                    def changedRepos = []
                    repos.each { repo ->
                        dir(repo.folder) {
                            if (fileExists("${repo.folder}.changed")) {
                                def val = readFile("${repo.folder}.changed").trim()
                                echo "📦 Changed file raw: '${val}' (len=${val.length()})"

                                // Normalize and parse to boolean
                                def isChanged = val?.toLowerCase() in ["true", "1", "yes"]
                                if (isChanged) {
                                    echo "📦 Changed repo ${repo.folder}"

                                    changedRepos << repo.folder

                                }
                            }
                        }
                    }

                    env.CHANGED_REPOS = changedRepos.join(',')
                    echo "📦 Changed repos: ${changedRepos.join(',')}"
                    echo "📦 Changed repos: ${env.CHANGED_REPOS}"
                }
            }
        }

        stage('Build Projects') {
            steps {
                script {
                    def parallelBuilds = [:]

                    repos.each { repo ->
                        parallelBuilds["Repo-${repo.folder}"] = {
                            if (!params.FORCE_BUILD_ALL && !isNewCommit(repo.folder, env.CHANGED_REPOS)) {
                                echo "⏭️ Skipping build for ${repo.folder}, no changes detected"
                                return
                            }

                            def vpsInfo = vpsInfos[repo.vpsRef]
                            dir(repo.folder) {
                                repo.envs.each { envConf ->
                                    def domain = extractDomain(envConf.MAIN_DOMAIN)

                                    if (isMissingCert(domain, env.MISSING_CERTS)) {
                                        echo "⏭️ Skipping build for ${envConf.name} (${domain}) due to missing cert"
                                        return
                                    }

                                    echo "=== Building ${repo.folder} branch >>${repo.branch}<< for environment: ${envConf.name} ==="

                                    withEnv(envConf.collect { k,v -> "${k.toUpperCase()}=${v}" } ) {
                                        sh '''
                                            if [ -f package.json ]; then
                                                export CI=true
                                                npm ci
                                                npx next build && npx next-sitemap

                                                if [ -d .next ]; then
                                                    rm -rf .next/cache || true
                                                    rm -rf .next/server || true
                                                    rm -rf .next/**/*.nft.json || true
                                                fi
                                            else
                                                echo "No package.json found, skipping build."
                                            fi
                                        '''

                                        def envOut = "outs/${envConf.name}"
                                        sh """
                                            mkdir -p outs
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
                                }
                            }
                        }
                    }

                    runWithMaxParallel(parallelBuilds, 3)  // 👈 cap parallelism
                }
            }
        }

        stage('Deploy Outs to VPS') {
            steps {
                script {
                    repos.each { repo ->
                        if (!params.FORCE_BUILD_ALL && !isNewCommit(repo.folder, env.CHANGED_REPOS)) {
                            echo "⏭️ Skipping deploy for ${repo.folder}, no changes detected"
                            return
                        }

                        def vpsInfo = vpsInfos[repo.vpsRef]
                        dir(repo.folder) {
                            repo.envs.each { envConf ->
                                def domain = extractDomain(envConf.MAIN_DOMAIN)

                                if (isMissingCert(domain, env.MISSING_CERTS)) {
                                    echo "⏭️ Skipping deploy for ${envConf.name} (${domain}) due to missing cert"
                                    return
                                }

                                def envOut = "outs/${envConf.name}"
                                echo "🚀 Deploying ${envOut} to ${repo.vpsHost}:${repo.webrootBase}/${envConf.name}"

                                sshagent (credentials: [repo.vpsCredId]) {
                                    sh """
                                        tar -czf ${envConf.name}.tar.gz -C outs/${envConf.name} .
                                        scp -o StrictHostKeyChecking=no ${envConf.name}.tar.gz ${repo.vpsUser}@${repo.vpsHost}:/tmp/

                                        ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} "
                                            sudo mkdir -p ${repo.webrootBase}/${envConf.name} &&
                                            sudo tar -xzf /tmp/${envConf.name}.tar.gz -C ${repo.webrootBase}/${envConf.name} &&
                                            rm /tmp/${envConf.name}.tar.gz &&
                                            sudo chown -R www-data:www-data ${repo.webrootBase}/${envConf.name}
                                        "
                                    """
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Generate NGNIX config and deploy SSH') {
            steps {
                script {
                    repos.each { repo ->
                        if (!params.FORCE_BUILD_ALL && !isNewCommit(repo.folder, env.CHANGED_REPOS)) {
                            echo "⏭️ Skipping nginx config for ${repo.folder}, no changes detected"
                            return
                        }

                        def vpsInfo = vpsInfos[repo.vpsRef]
                        dir(repo.folder) {
                            repo.envs.each { envConf ->
                                def domain = extractDomain(envConf.MAIN_DOMAIN)

                                if (isMissingCert(domain, env.MISSING_CERTS)) {
                                    echo "⏭️ Skipping nginx config for ${envConf.name} (${domain}) due to missing cert"
                                    return
                                }

                                def tmpConfigFile = "${envConf.name}.conf"
                                def nginxConfig = ngnixTemplate
                                    .replace('{{DOMAIN}}', domain)
                                    .replace('{{ENV_NAME}}', envConf.name)

                                writeFile(file: tmpConfigFile, text: nginxConfig)
                                echo "✅ Generated Nginx config for ${envConf.name} locally: ${tmpConfigFile}"
                                echo "📄 Local nginx config content for ${envConf.name}:\n${nginxConfig}"

                                sshagent(credentials: [repo.vpsCredId]) {
                                    sh """
                                        scp -o StrictHostKeyChecking=no ${tmpConfigFile} ${repo.vpsUser}@${repo.vpsHost}:/home/${repo.vpsUser}/${tmpConfigFile}
                                        ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} "
                                            sudo mv /home/${repo.vpsUser}/${tmpConfigFile} /etc/nginx/sites-available/${tmpConfigFile} &&
                                            sudo chown root:root /etc/nginx/sites-available/${tmpConfigFile} &&
                                            sudo ln -sf /etc/nginx/sites-available/${tmpConfigFile} /etc/nginx/sites-enabled/${tmpConfigFile} &&
                                            sudo nginx -t &&
                                            sudo systemctl reload nginx
                                        "
                                        ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} "cat /etc/nginx/sites-available/${tmpConfigFile}"
                                    """
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
