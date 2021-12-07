pipeline {
    agent any

    tools {
        maven 'apache-maven-latest'
        jdk 'openjdk-jdk11-latest'
    }

    stages {
        stage ('Build: Maven') {
            steps {
                sh 'mvn clean install -Pm2 -B' 
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
            // Record & publish checkstyle issues
            recordIssues  enabledForFailure: true, publishAllIssues: true,
            tool: checkStyle(reportEncoding: 'UTF-8'),
            qualityGates: [[threshold: 1, type: 'TOTAL', unstable: true]]

            // Record & publish test results
            withChecks('Tests') {
                junit '**/surefire-reports/*.xml'
            }

            // Record maven,java warnings
            recordIssues enabledForFailure: true, skipPublishingChecks:true, tools: [mavenConsole(), java()]
        }
    }
}