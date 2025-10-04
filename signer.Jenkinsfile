@Library('my-shared-library') _

// Utility: run tasks with capped parallelism
def runWithMaxParallel(tasks, maxParallel = 3) {
    def keys = tasks.keySet() as List
    def total = keys.size()

    for (int i = 0; i < total; i += maxParallel) {
        def slice = new ArrayList(keys.subList(i, Math.min(i + maxParallel, total)))
        def batch = [:]
        slice.each { k -> batch[k] = tasks[k] }
        parallel batch
    }
}

pipeline {
    agent any

    environment {
        // 👇 Tell Jenkins to use DinD instead of host socket
        DOCKER_BUILDKIT = "1"      // Enable BuildKit (faster, modern builds)
    }

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '100'))
    }

    parameters {
        booleanParam(
            name: 'FORCE_BUILD_ALL',
            defaultValue: false,
            description: 'Force build & deploy all repos, even if no new changes'
        )
    }

    triggers {
        cron('0,30 * * * *')
    }

    stages {
        stage('Check Docker Environment') {
            steps {
                // Ensure the required Docker client is accessible
                sh 'docker version'
            }
        }

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
                                    checkout([
                                        $class: 'GitSCM',
                                        branches: [[name: "*/${repo.branch}"]],
                                        userRemoteConfigs: [[
                                            url: repo.url,
                                            credentialsId: repo.credId
                                        ]],
                                        extensions: [
                                            [$class: 'CloneOption', depth: 1, noTags: true, shallow: true],
                                            [$class: 'PruneStaleBranch']
                                        ]
                                    ])
                                    redisState.addChangedRepo(repo.folder)
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
                                        echo "🔄 Changes in ${repo.folder}: ${oldCommit} → ${newCommit}"
                                        redisState.addChangedRepo(repo.folder)
                                    } else {
                                        echo "⏭️ No changes in ${repo.folder}"
                                    }
                                }
                            }
                        }
                    }

                    runWithMaxParallel(parallelTasks, 3)
                    def changedRepos = redisState.getChangedRepos() as List
                    echo "Collected repos = ${changedRepos}"

                    if (!params.FORCE_BUILD_ALL && changedRepos.isEmpty()) {
                        echo "⏭️ Nothing to build"
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                }
            }
        }

        stage('Check Certificates') {
            when {
                expression { params.FORCE_BUILD_ALL || !(redisState.getChangedRepos() as List).isEmpty() }
            }
            steps {
                script {
                    def reposToCheck = params.FORCE_BUILD_ALL ? repos : repos.findAll { r -> (redisState.getChangedRepos() as List).contains(r.folder) }
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
                                        echo "⚠️  Missing certificate for ${domain}"
                                        redisState.addMissingCert(domain)
                                    } else {
                                        echo "✅ Certificate exists for ${domain}"
                                    }
                                }
                            }
                        }
                    }

                    runWithMaxParallel(parallelTasks, 3)

                    if (redisState.getMissingCerts()) {
                        echo "⚠️ Missing certs: ${redisState.getMissingCerts()}"
                    } else {
                        echo "✅ All certs present"
                    }
                }
            }
        }

        stage('Build Projects') {
            steps {
                script {
                    def parallelBuilds = [:]

                    repos.each { repo ->
                        parallelBuilds["build-image-${repo.folder}"] = {
                            dir(repo.folder) {
                                if (!params.FORCE_BUILD_ALL && !redisState.isNewCommit(repo.folder)) {
                                    echo "⏭️ Skipping ${repo.folder}, no changes"
                                    return
                                }
                                def commitHash = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                                def imageTag = "${repo.imageName}:${commitHash}"

                                withCredentials([usernamePassword(
                                    credentialsId: 'ghcrCreds',
                                    usernameVariable: 'GHCR_USER',
                                    passwordVariable: 'GHCR_PAT'
                                )]) {
                                    sh """
                                        tree -a -L 2 || find . -mindepth 2 -type f
                                    """

                                    sh """
                                        echo \$GHCR_PAT | docker login ghcr.io -u \$GHCR_USER --password-stdin
                                    """
                                    sh "docker buildx prune -f " // optional, clears local cache

                                    // build per arch
                                    sh "docker buildx build --no-cache --pull --platform linux/amd64 -t ghcr.io/$GHCR_USER/${imageTag}-amd64 . --push"
                                    sh "docker buildx build --no-cache --pull --platform linux/arm64 -t ghcr.io/$GHCR_USER/${imageTag}-arm64 . --push"

                                    // create and push manifest
                                    sh """
                                        docker manifest create ghcr.io/$GHCR_USER/${imageTag} \
                                            --amend ghcr.io/$GHCR_USER/${imageTag}-amd64 \
                                            --amend ghcr.io/$GHCR_USER/${imageTag}-arm64 || true
                                        docker manifest push ghcr.io/$GHCR_USER/${imageTag} || true
                                    """
                                }
                            }
                        }
                    }

                    runWithMaxParallel(parallelBuilds, 1) // single agent to push at time 
                }
            }
        }

        stage('Refresh image docker vps') {
            when {
                expression { params.FORCE_BUILD_ALL || !(redisState.getChangedRepos() as List).isEmpty() }
            }
            steps {
                script {
                    def reposToCheck = params.FORCE_BUILD_ALL ? repos : repos.findAll { r -> (redisState.getChangedRepos() as List).contains(r.folder) }
                    def parallelTasks = [:]

                    reposToCheck.each { repo ->
                        parallelTasks["refresh-${repo.folder}"] ={
                            dir(repo.folder) {

                                withCredentials([usernamePassword(
                                    credentialsId: 'ghcrCreds',
                                    usernameVariable: 'GHCR_USER',
                                    passwordVariable: 'GHCR_PAT'
                                )]) {
                                    def commitHash = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                                    def imageTag = "${repo.imageName}:${commitHash}"

                                    def vpsInfo = vpsInfos[repo.vpsRef]
                                    sshagent (credentials: [vpsInfo.vpsCredId]) {
sh """
ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} <<EOF
docker stop ${repo.imageName} || true
docker rm ${repo.imageName} || true
docker pull ghcr.io/$GHCR_USER/${imageTag}
docker run -d \\
    --name ${repo.imageName} \\
    --restart unless-stopped \\
    -p ${repo.imagePort}:${repo.imagePort} \\
    -v /home/ubuntu/signer/keys:/usr/src/app/keys \\
    ghcr.io/$GHCR_USER/${imageTag}
sudo nginx -t &&
sudo systemctl reload nginx
EOF
"""

                                    }
                                }
                            }

                        }
                    }

                    runWithMaxParallel(parallelTasks, 1)

                }
            }
        }

    }
}
