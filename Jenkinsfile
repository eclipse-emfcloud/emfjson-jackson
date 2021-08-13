pipeline {
    agent any

    tools {
        maven 'apache-maven-latest'
        jdk 'openjdk-jdk11-latest'
    }

    stages {
        stage ('Build') {
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
}