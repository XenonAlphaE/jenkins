// This annotation tells Jenkins to load the library you configured
@Library('my-shared-library') _ 



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

    options {
        disableConcurrentBuilds()   // 🚫 no concurrent runs
        buildDiscarder(logRotator(numToKeepStr: '100')) // optional cleanup
        // timeout(time: 60, unit: 'MINUTES')            // optional safety
    }


    triggers {
        cron('0,30 * * * *')
    }


    stages {
        stage('Clear redis') {
            steps {
                script {
                    try {
                        redisState.clearAll()
                    } catch (Exception e) {
                        echo "redisState not found: ${e}"
                    }
                }
            }
        }



        stage('Load Script') {
            steps {
                script {
                    repos = load "gitRepos/priceSignature.groovy"
                    vpsInfos = load 'vps.groovy'

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
                                    redisState.addChangedRepo(repo.folder)
                                    // changedRepos << repo.folder
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

                                        // changedRepos << repo.folder
                                        redisState.addChangedRepo(repo.folder)

                                    } else {
                                        echo "⏭️ No changes in ${repo.folder}"


                                    }
                                }

                            }

                        }
                    }

                    runWithMaxParallel(parallelTasks, 3)  // 👈 cap parallelism
                    
                    def changedRepos = redisState.getChangedRepos() as List

                    echo "Collected repos = ${changedRepos}"
                    if (!params.FORCE_BUILD_ALL && changedRepos.isEmpty()) {
                        echo "⏭️ No changes and FORCE_BUILD_ALL not set, stopping pipeline early."
                        return  // exits this stage, and since no later stages run → SUCCESS
                    }
                }
            }
        }

        stage('Check Certificates') {
            when { expression { 
                def changedRepos = redisState.getChangedRepos() as List
                return params.FORCE_BUILD_ALL || !changedRepos.isEmpty() 
            } }

            steps {
                script {
                    def changedRepos = redisState.getChangedRepos() as List
                    def reposToCheck = params.FORCE_BUILD_ALL ? repos : repos.findAll { r -> changedRepos.contains(r.folder) }
                    def parallelTasks = [:]

                    reposToCheck.each { repo ->
                        def vpsInfo = vpsInfos[repo.vpsRef]
                        repo.envs.each { site ->
                            parallelTasks["check-${site.MAIN_DOMAIN}"] = {

                                def domain = commonUtils.extractDomain(site.MAIN_DOMAIN)

                                sshagent (credentials: [vpsInfo.vpsCredId]) {
                                    def exists = sh(
                                        script: """

                                            ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} \
                                            "sudo test -f /etc/letsencrypt/live/${domain}/fullchain.pem && echo yes || echo no"
                                        """,
                                        returnStdout: true
                                    ).trim()

                                    if (exists == "no") {
                                        echo "⚠️  Certificate missing for ${domain}"
                                        redisState.addMissingCert(domain)
                                    } else {
                                        echo "✅ Certificate exists for ${domain}"
                                    }
                                }
                            }
                        }
                    }

                    runWithMaxParallel(parallelTasks, 3)  // 👈 cap parallelism

                    if (redisState.getMissingCerts()) {
                        echo "⚠️  Some certificates are missing: ${redisState.getMissingCerts()}"
                    } else {
                        echo "✅ All certificates present"
                    }
                }
            }
        }

        stage('Build Projects') {
            steps {
                script {

                    def parallelBuilds = [:]

                    repos.each { repo ->
                        repo.envs.eachWithIndex { envConf, idx ->
                            parallelBuilds["build-${envConf.name}"] = {
                                if (!params.FORCE_BUILD_ALL && !redisState.isNewCommit(repo.folder)) {
                                    echo "⏭️ Skipping setup for ${repo.folder}, no changes detected"
                                    return
                                }
                                withCredentials([usernamePassword(credentialsId: 'ghcrCreds', usernameVariable: 'GHCR_USER', passwordVariable: 'GHCR_PAT')]) {
                                    sh """
                                        echo $GHCR_PAT | docker login ghcr.io -u $GHCR_USER --password-stdin
                                        docker buildx build --no-cache --platform linux/amd64,linux/arm64 -t ghcr.io/$GHCR_USER/${repo.imageName}:latest --push .
                                    """
                                }
                            }
                        }
                    }

                    runWithMaxParallel(parallelBuilds, 3)  // 👈 cap parallelism
                }
            }
        }
    }
}