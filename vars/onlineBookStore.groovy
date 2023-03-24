def call(repoUrl,dockerImageName){
    pipeline {
        agent any
        tools {
            maven 'Maven 3.9.0'
        }
        environment {
            DOCKERHUB_CREDENTIALS = credentials('dockerhubID')
        }
        stages {
            stage("git_checkout") {
                steps {
                    git branch: 'J2EE', credentialsId: 'githubloginID', url:"${repoUrl}"
                    echo "repo cloned successfully"
                }
            }
            stage("Build") {
                steps {
                    sh 'mvn clean package'
                }
                post{
                    success{
                       echo "Archiving the artifacts"
                       archiveArtifacts artifacts: '**/target/*.war'
                   }
                }
            }
            stage("Build Docker Image") {
                steps {
                    script {
                        sh 'docker build -t "${dockerImageName}" .'
                    }
                }
            }
            stage("Push image to Dockerhub") {
                steps {
                    script {
                        sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --passwd-stdin'
                        sh 'docker push "${dockerImageName}"'
                    }
                }
            }
        }
    }  
}
