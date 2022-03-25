@Library('smsoft-libs')_

pipeline {
    options {
		disableConcurrentBuilds()
	}
	agent any
	tools {
		maven 'mvn'
		jdk 'jdk-15.0.1'
	}
	environment {
        namespace = 'etp'
        cd_registry_cred = credentials('cd-registry-credentials')
        cd_registry_url = 'cd-registry.cls.sm-soft.ru'
        registry_project = 'etp'
		project = 'EFP.PFO.RestApi'
        version = '1.0'
        app = 'pfo-rest-api'
        project_ui = 'EFP.PFO.UI'
        version_ui = '1.1'
        app_ui = 'pfo-ui'
        kubernetes_folder='kubernetes'
    }
    parameters {
            choice(choices: ['all', 'build_rest', 'build_ui'],
                    description: 'Choose what you need to build and deploy',
                    name: 'build_type')
    }
	stages {
		stage('Initialize') {
			steps {
				sh '''
					echo "PATH = ${PATH}"
					echo "M2_HOME = ${M2_HOME}"
				   '''
			}
		}
		stage("[PFO Root] build") {
	    	when {
	    	    expression { branch "pfo_ui" }
	    	    anyOf {
                    environment name: 'build_type', value: 'all'
                    environment name: 'build_type', value: 'build_rest'
                }
	    	 }
			steps {
				sh """
				mvn -f pom.xml clean install
				"""
			}
		}
		stage("[PFO RestApi] docker build") {
	    	when {
	    	    expression { branch "develop" }
	    	    anyOf {
                    environment name: 'build_type', value: 'all'
                    environment name: 'build_type', value: 'build_rest'
                }
	    	 }

			steps {
				sh """
				cd ${project}
				docker image build -t ${app}:${version}-${BUILD_NUMBER} .
				docker login -u ${cd_registry_cred_USR} -p ${cd_registry_cred_PSW} ${cd_registry_url}
                docker tag ${app}:${version}-${BUILD_NUMBER} ${cd_registry_url}/${registry_project}/${app}:${version}-${BUILD_NUMBER}
                docker push ${cd_registry_url}/${registry_project}/${app}:${version}-${BUILD_NUMBER}
				"""
			}
            post {
		        always {
		            sh "docker logout ${cd_registry_url}"
		        }
			}
		}
		stage("[PFO RestApi] Add config map") {
	    	when {
	    	    expression { branch "develop" }
	    	    anyOf {
                    environment name: 'build_type', value: 'all'
                    environment name: 'build_type', value: 'build_rest'
                }
	    	 }
            agent {
                docker {
                    image'openshift/origin-cli'
                    args '-u root:root'
                }
            }
            steps {
                ocAddCMManifest(app, project+'/'+kubernetes_folder, registry_project)
            }
        }
		stage("[PFO RestApi] Deploy to OKD") {
	    	when {
	    	    expression { branch "develop" }
	    	    anyOf {
                    environment name: 'build_type', value: 'all'
                    environment name: 'build_type', value: 'build_rest'
                }
	    	 }
            agent {
                docker {
                    image'openshift/origin-cli'
                    args '-u root:root'
                }
            }
            steps {
                ocDeploy(project+'/'+kubernetes_folder, registry_project, app, cd_registry_url, version)
            }
        }
        stage("[PFO RestApi] Add SVC") {
	    	when {
	    	    expression { branch "develop" }
	    	    anyOf {
                    environment name: 'build_type', value: 'all'
                    environment name: 'build_type', value: 'build_rest'
                }
	    	 }
            agent {
                docker {
                    image'openshift/origin-cli'
                    args '-u root:root'
                }
            }
            steps {
                ocAddSvc(app, project+'/'+kubernetes_folder, registry_project)
            }
        }
        stage("[PFO RestApi] Add VS") {
	    	when {
	    	    expression { branch "develop" }
	    	    anyOf {
                    environment name: 'build_type', value: 'all'
                    environment name: 'build_type', value: 'build_rest'
                }
	    	 }
            agent {
                docker {
                    image'openshift/origin-cli'
                    args '-u root:root'
                }
            }
            steps {
                ocAddVs(app, project+'/'+kubernetes_folder, registry_project)
            }
        }

        //////////////

		stage("[PFO UI] build") {
	    	when {
	    	    expression { branch "pfo_ui" }
	    	    anyOf {
                    environment name: 'build_type', value: 'all'
                    environment name: 'build_type', value: 'build_ui'
                }
	    	}
			steps {
				sh """
				cd ${project_ui}
				mvn -f pom.xml clean install
				"""
			}
		}
		stage("[PFO UI] docker build") {
	    	when {
	    	    expression { branch "pfo_ui" }
	    	    anyOf {
                    environment name: 'build_type', value: 'all'
                    environment name: 'build_type', value: 'build_ui'
                }
	    	 }
			steps {
				sh """
				cd ${project_ui}
				docker image build -t ${app_ui}:${version_ui}-${BUILD_NUMBER} .
				docker login -u ${cd_registry_cred_USR} -p ${cd_registry_cred_PSW} ${cd_registry_url}
                docker tag ${app_ui}:${version_ui}-${BUILD_NUMBER} ${cd_registry_url}/${registry_project}/${app_ui}:${version_ui}-${BUILD_NUMBER}
                docker push ${cd_registry_url}/${registry_project}/${app_ui}:${version_ui}-${BUILD_NUMBER}
				"""
			}
            post {
		        always {
		            sh "docker logout ${cd_registry_url}"
		        }
			}
		}
		stage("[PFO UI] Add config map") {
		    when {
                expression { branch "pfo_ui" }
            	    anyOf {
                        environment name: 'build_type', value: 'all'
                        environment name: 'build_type', value: 'build_ui'
                    }
            }
            agent {
                docker {
                    image'openshift/origin-cli'
                    args '-u root:root'
                }
            }
            steps {
                ocAddCMManifest(app_ui, project_ui+'/'+kubernetes_folder, registry_project)
            }
        }
		stage("[PFO UI] Deploy to OKD") {
		    when {
                expression { branch "pfo_ui" }
            	    anyOf {
                        environment name: 'build_type', value: 'all'
                        environment name: 'build_type', value: 'build_ui'
                    }
            }
            agent {
                docker {
                    image'openshift/origin-cli'
                    args '-u root:root'
                }
            }
            steps {
                ocDeploy(project_ui+'/'+kubernetes_folder, registry_project, app_ui, cd_registry_url, version_ui)
            }
        }
        stage("[PFO UI] Add SVC") {
		    when {
                expression { branch "pfo_ui" }
            	anyOf {
                    environment name: 'build_type', value: 'all'
                    environment name: 'build_type', value: 'build_ui'
                }
            }
            agent {
                docker {
                    image'openshift/origin-cli'
                    args '-u root:root'
                }
            }
            steps {
                ocAddSvc(app_ui, project_ui+'/'+kubernetes_folder, registry_project)
            }
        }
        stage("[PFO UI] Add VS") {
		    when {
                expression { branch "pfo_ui" }
            	anyOf {
                    environment name: 'build_type', value: 'all'
                    environment name: 'build_type', value: 'build_ui'
                }
            }
            agent {
                docker {
                    image'openshift/origin-cli'
                    args '-u root:root'
                }
            }
            steps {
                ocAddVs(app_ui, project_ui+'/'+kubernetes_folder, registry_project)
            }
        }


	post {
		success {
			archiveArtifacts artifacts:'**/target/*.jar,**/target/*.war', fingerprint: true
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


