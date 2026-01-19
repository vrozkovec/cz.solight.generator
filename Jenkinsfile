def targetServer = 'latudio-app' 
def useProxy = false
def proxyServer = 'TODO' 

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
						useProxy: useProxy,
						proxyServer: proxyServer
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