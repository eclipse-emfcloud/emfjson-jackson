pipeline {
    agent any

    tools {
        maven 'apache-maven-latest'
        jdk 'openjdk-jdk11-latest'
    }

    stages {
        stage ('Build: Plain Maven (M2)') {
            steps {
                // ignore test failures since we parse the test results afterwards
                sh 'mvn clean install -Pm2 -B -Dmaven.test.failure.ignore=true' 
            }
        }
        
        stage ('Build: Eclipse-based (P2)') {
            steps {
            	// ignore test failures since we parse the test results afterwards
            	sh 'mvn clean install -Pm2 -B -Dmaven.test.failure.ignore=true' 
                dir('emfjson-p2-build') {
                	sh 'mvn p2:site' 
                }
            }
        }
        
        stage('Deploy') {
            when { branch 'master' }
            steps {
            	parallel(
            	    p2: {
            	        build job: 'deploy-emfcloud-emfjson-jackson-p2', wait: false
            	    },
            	    m2: {
            	        build job: 'deploy-emfcloud-emfjson-jackson-m2', wait: false
            	    }
            	)
            }
        }
        
    }
    post {
        always {
            archiveArtifacts artifacts: 'emfjson-p2-build/target/repository/**', fingerprint: true
        }
    }
}