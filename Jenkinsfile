pipeline {
    agent any
     tools {
        jdk 'jdk_1.8'
        gradle 'gradle_4.3.1'
    }

    stages {
        stage('Build') {
            steps {
                echo 'Building..'
                withGradle {
                    sh '''
                    gradle clean build -x test
                    '''
                }
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..'
                sh 'gradle test'
            }
        }
        stage('SCA') {
            steps {
                echo 'Dependency Check....'
                sh 'gradle dependencyCheckAnalyze'
            }
        }
        stage('SonarQube') {
            steps {
                script {
                    def scannerHome = tool 'sonar_scanner_4.6'
                    withSonarQubeEnv('Sonar Server'){
                        sh '${scannerHome}/bin/sonar-scanner'
                    }
                }
            }
        }
    }
}