@Library('smsoft-libs') _

pipeline {
    options {
        buildDiscarder logRotator(numToKeepStr: '3')
        disableConcurrentBuilds()
        ansiColor('xterm')
    }
    agent any
    tools {
        maven 'mvn-3.6.3'
        jdk 'jdk-17'
        // avaliable jdk1.8.0_91, jdk9.0.4, jdk10.0.1, jdk-14.0.1, jdk11.0.2, jdk-13.0.2
    }
    environment {
        commiter_name = sh(script: 'printf $(git show -s --pretty=%an)', returnStdout: true)
        cd_registry_cred = credentials('cd-registry-credentials')
        cd_registry_url = 'cd-registry.cls.sm-soft.ru'
        appDir = "${WORKSPACE}"
        // app = "${env.GIT_URL.replaceAll('https://github.com/mbuyakov/', '').replaceAll('.git', '').toLowerCase().replace(".","-").replace("_","-")}-adapter"
        app = "efp-oati-order-051001-adapter" // hardcode app name if it isn't correct from repo name
        registry_project = 'etp'
        version = '1.0'
        test_service_cred = credentials('efp-testing-service')
    }

    parameters {
        choice(choices: ['all', 'build_and_deploy_app', 'update_or_publish_api', 'test_app'],
                description: 'Choose what you need to build and deploy',
                name: 'build_type')
    }

    stages {
        stage('Initialize') {
            steps {
                colorLogger("PATH = ${PATH}", "info")
                colorLogger("M2_HOME = ${M2_HOME}", "info")
                colorLogger("commiter_name = ${commiter_name}", "info")
            }
        }
        stage('Check repository config') {
            when {
                expression { branch "develop" }
            }
            steps {
                saveRepositoryConfig()
            }
        }
        stage('build and deploy application') {
            when { branch "develop" }
                environment {
                    appDir = "${WORKSPACE}"  //added for example when repo contains multiple project
                    // app = "${env.GIT_URL.replaceAll('https://github.com/mbuyakov/', '').replaceAll('.git', '').toLowerCase().replace(".","-").replace("_","-")}-adapter"
                    app = "efp-oati-order-051001-adapter" // hardcode app name if it isn't correct from repo name
                    vs = fileExists "${app}-vs.yml"
                    svc = fileExists "${app}-svc.yml"
                }
                stages {
                    stage('Check libs versions') {
                       when {
                            expression { branch "develop" }
                            anyOf {
                                environment name: 'build_type', value: 'all'
                                environment name: 'build_type', value: 'build_and_deploy_app'
                            }
                        }
                        steps {
                            checkVersion()
                        }
                    }
                    stage('build application') {
                        when {
                            expression { branch "develop" }
                            anyOf {
                                environment name: 'build_type', value: 'all'
                                environment name: 'build_type', value: 'build_and_deploy_app'
                            }
                        }
                        steps {
                            sh """
                                mvn -s ${appDir}/nexus-settings.xml -f ${appDir}/pom.xml clean install
                                mvn sonar:sonar -Dsonar.host.url=http://sonarqube.adm.sm-soft.ru || true
                            """
                        }
                    }
                    stage('prepare folder') {
                        when {
                            expression { branch "develop" }
                            anyOf {
                                environment name: 'build_type', value: 'all'
                                environment name: 'build_type', value: 'build_and_deploy_app'
                            }
                        }
                        steps {
                            sh """
                            echo "appDir = ${appDir}"
                            cd ${appDir}
                            rm -rf docker/efp-core/buildfiles/*.jar
                            rm -rf docker/efp-core/buildfiles/efp-core-lib/*
                            echo "copy build artifact"
                            cp target/alternateLocation/*.jar docker/efp-core/buildfiles/
                            cp target/*.jar docker/efp-core/buildfiles/efp-core-lib/

                            ls -la docker/efp-core/buildfiles/
                            ls -la docker/efp-core/buildfiles/efp-core-lib/

                            tar czvf ${app}.tgz docker/*
                        """
                        }
                    }
                    stage('build image') {
                        when {
                            expression { branch "develop" }
                            anyOf {
                                environment name: 'build_type', value: 'all'
                                environment name: 'build_type', value: 'build_and_deploy_app'
                            }
                        }
                        steps {
                            sh """
                            cd ${appDir}/docker/efp-core/buildfiles
                            docker image build -t ${app}:${version}-${BUILD_NUMBER} .
                            docker login -u ${cd_registry_cred_USR} -p ${cd_registry_cred_PSW} ${cd_registry_url}
                            docker tag ${app}:${version}-${BUILD_NUMBER} ${cd_registry_url}/${registry_project}/${app}:${version}-${BUILD_NUMBER}
                            docker push ${cd_registry_url}/${registry_project}/${app}:${version}-${BUILD_NUMBER}
                        """
                        }
                        post {
                            always {
                                sh """
                                docker logout ${cd_registry_url}
                            """
                            }
                        }
                    }
                    stage('openshift deployment') {
                        when {
                            expression { branch "develop" }
                            anyOf {
                                environment name: 'build_type', value: 'all'
                                environment name: 'build_type', value: 'build_and_deploy_app'
                            }
                        }
                        agent {
                            docker {
                                image 'openshift/origin-cli'
                                args '-u root:root'
                            }
                        }
                        environment {
                        appDir = "${WORKSPACE}"
                         }
                        stages {
                            stage('add configmap') {
                                when {
                                    expression { branch "develop" }
                                    anyOf {
                                        environment name: 'build_type', value: 'all'
                                        environment name: 'build_type', value: 'build_and_deploy_app'
                                    }
                                }
                                steps {
                                    ocAddCMManifest(app, appDir, registry_project)
                                }
                            }
                            stage('deploy to okd') {
                                when {
                                    expression { branch "develop" }
                                    anyOf {
                                        environment name: 'build_type', value: 'all'
                                        environment name: 'build_type', value: 'build_and_deploy_app'
                                    }
                                }
                                steps {
                                    ocDeploy(appDir, registry_project, app, cd_registry_url, version)
                                }
                                post {
                                    failure {
                                        ocRollback(appDir, registry_project, app)
                                    }
                                }
                            }
                            stage('add svc') {
                                when {
                                    expression { branch "develop" }
                                    expression { svc == 'true' }
                                    anyOf {
                                        environment name: 'build_type', value: 'all'
                                        environment name: 'build_type', value: 'build_and_deploy_app'
                                    }
                                }
                                steps {
                                    ocAddSvc(app, appDir, registry_project)
                                }

                            }
                            stage('add vs') {
                                when {
                                    expression { branch "develop" }
                                    expression { vs == 'true' }
                                    anyOf {
                                        environment name: 'build_type', value: 'all'
                                        environment name: 'build_type', value: 'build_and_deploy_app'
                                    }
                                }
                                steps {
                                    ocAddVs(app, appDir, registry_project)
                                }
                            }
                        }
                    }
                     stage('archive artifact') {
                        when {
                            expression { branch "develop" }
                            anyOf {
                                environment name: 'build_type', value: 'all'
                                environment name: 'build_type', value: 'build_and_deploy_app'
                            }
                        }
                        steps {
                            colorLogger("stage: archive artifact ${appDir}", "info")
                            archiveArtifacts artifacts: "**/${app}.tgz", fingerprint: true
                        }
                     }
                     stage('update_or_publish_api') {
                          when {
                              branch 'develop'
                          }
                          steps {
                              updateOrPublishApiWithFile()
                          }
                      }
                     stage('send robot conf') {
                          when { expression { fileExists "${appDir}/test"} }
                              steps {
                                  loadRobotConfig(test_service_cred_USR, test_service_cred_PSW)
                              }
                      }
                      stage('start robot') {
                          steps {
                              startRobot(test_service_cred_USR, test_service_cred_PSW, "051001")
                          }
                      }
                     stage('Tests') {
                         when {
                             expression { branch "develop" }
                                 anyOf {
                                     environment name: 'build_type', value: 'all'
                                     environment name: 'build_type', value: 'test_app'
                                 }
                             }
                         steps {
                             runEfpTestCollection("051001", "always_set_colletions.json")
                         }
                    }
                }
            }
                //this stage added via PFO-UI

        stage('Send PFO') {
            when {
                expression { branch "undefined" }
                anyOf {
                    environment name: 'build_type', value: 'all'
                    environment name: 'build_type', value: 'load_pfo'
                }
            }
            steps {
                sendPfo()
            }
        }
    }


        post {
            failure {
                telegramSendNotification_v2()
            }
            unstable {
                telegramSendNotification_v2("@DanilObyedkov ")
            }
        }
    }
