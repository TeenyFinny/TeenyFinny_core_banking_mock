pipeline {
    agent any

    environment {
        REGISTRY   = 'docker.io'
        MAIN_IMAGE_NAME = 'TeenyFinny/core'
        DEV_IMAGE_NAME = 'TeenyFinny/coretest'
    }

    stages {
        stage('CI : checkout') {
            steps {
                echo '빌드 시작됨!'
                cleanWs()
                sshagent(credentials: ['github-core-banking']) { // Jenkins Credentials ID
                    sh '''
                    GIT_SSH_COMMAND="ssh -o StrictHostKeyChecking=no" \
                    git clone --depth=1 --branch main git@github.com:TeenyFinny/TeenyFinny_core_banking_mock.git .
                    '''
                }
            }
        }

        stage('CI : build') {
            steps{
                sh '''
                set -euxo pipefail
                chmod +x ./gradlew

                ./gradlew clean build
                '''
            }
        }

        stage('CI : SonarQube analysis') {
            steps {
                withSonarQubeEnv('sonarqube-server') {
                    sh '''
                    set -euxo pipefail
                    chmod +x ./gradlew

                    echo '===== sonarqube 서버, 토큰 유효성 검증 ====='
                    curl http://sonarqube:9000/api/server/version
                    curl -sS -u "$SONAR_AUTH_TOKEN": http://sonarqube:9000/api/authentication/validate
                    curl -sS -u "$SONAR_AUTH_TOKEN": "http://sonarqube:9000/api/projects/search?projects=test"

                    ./gradlew sonar -Dsonar.token=$SONAR_AUTH_TOKEN -Dsonar.host.url=$SONAR_HOST_URL
                    '''
                }
            }
        }

        stage('CI : Quality Gate') {
            steps{
                timeout(time: 1, unit: 'MINUTES') {
                    script{
                        echo "Start"
                        def qg = waitForQualityGate()
                        echo "Status: ${qg.status}"
                        if(qg.status != 'OK') {
                            echo "NOT OK Status: ${qg.status}"
                            error "Pipeline aborted due to quality gate failure: ${qg.status}"
                        } else{
                            echo "status: ${qg.status}"
                        }
                        echo "End"
                    }
                }
            }
        }

        stage('CD : push to docker hub') {
        		steps {
        		  withCredentials([usernamePassword(credentialsId: 'docker-hub',
        		                                    usernameVariable: 'REG_USER',
        		                                    passwordVariable: 'REG_PASS')]) {
        		    sh(label: 'Docker login', script: 'echo "$REG_PASS" | docker login $REGISTRY -u "$REG_USER" --password-stdin')
        		    script {
                        if (branch == 'test/jenkins' || branch == 'origin/test/jenkins') {
                            sh(label: 'Docker build & push (latest)', script: '''
                              set -euxo pipefail
                              docker build -t ${DEV_IMAGE_NAME}:latest .
                              docker push  ${DEV_IMAGE_NAME}:latest
                            ''')
                        }

                        if (branch == 'dev' || branch == 'origin/dev') {
                            sh(label: 'Docker build & push (latest)', script: '''
                              set -euxo pipefail
                              docker build -t ${DEV_IMAGE_NAME}:latest .
                              docker push  ${DEV_IMAGE_NAME}:latest
                            ''')
                        }

                        if (branch == 'main' || branch == 'origin/main') {
                            sh(label: 'Docker build & push (latest)', script: '''
                              set -euxo pipefail
                              docker build -t ${MAIN_IMAGE_NAME}:latest .
                              docker push  ${MAIN_IMAGE_NAME}:latest
                            ''')
                        }
                    }
        		  }
        		}
        	}

    } // end of stages
} // end of pipeline
