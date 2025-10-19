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


    parameters {
        booleanParam(
            name: 'FORCE_BUILD_ALL',
            defaultValue: false,
            description: 'Force build & deploy all repos, even if no new changes'
        )
        booleanParam(
            name: 'REMOVE_ALL_NGINX',
            defaultValue: false,
            description: 'Force remove all nginx on build and replace enable sites only'
        )
        string(name: 'PARENT_BUILD', description: 'name of trigger from build pipeline')
    }




    stages {
        stage('Load Script') {
            steps {
                script {
                    repos = load 'repos.groovy'
                    vpsInfos = load 'vps.groovy'

                }
            }
        }

        stage('Generate nginx config and deploy SSH') {
            steps {
                script {
                    def changedRepos = redisState.getChangedRepos(params.PARENT_BUILD)
                    def parallelTasks = [:]

                    if (!params.FORCE_BUILD_ALL && !changedRepos) {
                        echo "⏭️ Skipping nginx reload, no changes detected"
                        return
                    }
                    
                    if (params.REMOVE_ALL_NGINX ) {
                        echo "⏭️ Remove all sites before place new enable sites."
                        vpsInfos.values().each { vpsConf -> 
                            sshagent(credentials: [vpsConf.vpsCredId]) {
                                sh """
                                    ssh -o StrictHostKeyChecking=no ${vpsConf.vpsUser}@${vpsConf.vpsHost} "
                                        sudo rm -f /etc/nginx/sites-enabled/*
                                    "
                                """
                            }
                        }
                    }

                    repos.each { repo ->
                        repo.envs.each { envConf ->
                            parallelTasks["nginx-${envConf.name}"] = {
                                nginxUtils.generate(repo, envConf, vpsInfos )
                            }
                        }
                    }

                    runWithMaxParallel(parallelTasks, 2)  // 👈 cap parallelism

                    vpsInfos.values().each { vpsConf -> 
                        sshagent(credentials: [vpsConf.vpsCredId]) {
                            sh """
                                ssh -o StrictHostKeyChecking=no ${vpsConf.vpsUser}@${vpsConf.vpsHost} "

                                    sudo nginx -t &&

                                    sudo systemctl reload nginx
                                "
                            """
                        }
                    }
                }
            }
        }

    }
}
