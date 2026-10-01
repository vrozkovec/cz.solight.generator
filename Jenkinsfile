def targetServer = 'latudio-app' 

// master (production) is built and tested on every push but deployed only by the nightly build
// inside the deployment window (when there are new commits), for an undeployed commit whose
// message contains "*hotfix*", or when the build is started manually
def plannedDeployment = env.BRANCH_NAME == 'main'
def deploymentEnabledFrom = 19 // whole hour 0-23, inclusive
def deploymentEnabledTo = 6 // whole hour 0-23, exclusive; the window may span midnight
def deploymentTimeZone = 'Europe/Prague'



pipeline {
    agent any
    options { 
		disableConcurrentBuilds() 
	}
    stages {
        stage('Build') {
            steps {
                stageBuild()
            }
        }
/*        
		Careful!
		
		When enabling test that runs the app, make sure import does not query Deepl all over again.
		
        stage('Test') {
            steps {
                stageTest()
            }
            post {
                always {
                    junit 'target/surefire-reports.xml'
                }
            }
        }
*/
        stage('Copy artifacts') {
            steps {
				copyToDeployDir()
            }
        }
        stage('Deploy') {
            steps {
				ansiColor('xterm') {
					runAnsibleDeployment (
						targetServer: targetServer,
						plannedDeployment: plannedDeployment,
						deploymentEnabledFrom: deploymentEnabledFrom,
						deploymentEnabledTo: deploymentEnabledTo,
						deploymentTimeZone: deploymentTimeZone
					)
				}	
            }
        }
    }
	tools {
		maven 'M3'
		jdk 'temurin-17'
	}
}