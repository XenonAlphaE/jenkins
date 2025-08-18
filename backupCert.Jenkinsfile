pipeline {
    agent any

    
    environment {
        BACKUP_DIR = "certbot_backups"   // just folder name
        DATE_SUFFIX = "${new Date().format('yyyy-MM-dd')}"
    }

    triggers {
        cron('H 2 * * *')
    }

    options {
        disableConcurrentBuilds()   // 🚫 no concurrent runs
        // buildDiscarder(logRotator(numToKeepStr: '10')) // optional cleanup
        // timeout(time: 60, unit: 'MINUTES')            // optional safety
    }



    stages {

        stage('Load Script') {
            steps {
                script {
                    vpsInfos = load 'vps.groovy'
                }
            }
        }

        stage('Backup Certbot on all VPS') {
            steps {
                script {
                    vpsInfos.each { vpsKey, vps ->
                        sshagent(credentials: [vps.vpsCredId]) {
                            // Create backup on remote VPS (store under user's $HOME)
                            sh """
                                ssh -o StrictHostKeyChecking=no ${vps.vpsUser}@${vps.vpsHost} '
                                    mkdir -p /home/${vps.vpsUser}/${BACKUP_DIR} &&
                                    sudo tar -czf /home/${vps.vpsUser}/${BACKUP_DIR}/certbot-backup-${DATE_SUFFIX}.tar.gz /etc/letsencrypt
                                '
                            """

                            // Download backup into Jenkins workspace
                            sh """
                                scp -o StrictHostKeyChecking=no ${vps.vpsUser}@${vps.vpsHost}:/home/${vps.vpsUser}/${BACKUP_DIR}/certbot-backup-${DATE_SUFFIX}.tar.gz \
                                certbot-backup-${vpsKey}-${DATE_SUFFIX}.tar.gz
                            """
                        }

                        echo "✅ Backup completed and downloaded for VPS: ${vpsKey}"
                    }
                }
            }
        }


        // stage('Restore Latest Backup') {
        //     when { expression { return params.RESTORE_VPS != 'none' } }
        //     steps {
        //         script {
        //             def vps = vpsInfos[params.RESTORE_VPS]
        //             sshagent(credentials: [vps.vpsCredId]) {
        //                 sh """
        //                     ssh -o StrictHostKeyChecking=no ${vps.vpsUser}@${vps.vpsHost} '
        //                         LATEST=\$(ls -1t ${BACKUP_DIR}/certbot-backup-*.tar.gz | head -n1) &&
        //                         if [ -f "\$LATEST" ]; then
        //                             echo "Restoring \$LATEST on ${params.RESTORE_VPS} ..."
        //                             sudo tar -xzf "\$LATEST" -C / &&
        //                             sudo chown -R root:root /etc/letsencrypt &&
        //                             sudo chmod -R 700 /etc/letsencrypt
        //                         else
        //                             echo "⚠️ No backup found to restore."
        //                         fi
        //                     '
        //                 """
        //             }
        //             echo "✅ Restore complete for VPS: ${params.RESTORE_VPS}"
        //         }
        //     }
        // }
    }
}
