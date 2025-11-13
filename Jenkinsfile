pipeline {
    agent any

    stages {

        stage('Build Start') {
            steps {
                echo '빌드 시작됨!'

                script {
                    // 멀티브랜치면 BRANCH_NAME, 일반 파이프라인이면 GIT_BRANCH에 들어가는 경우가 많아서 둘 다 확인
                    def branch = env.GIT_BRANCH

                    if (branch == 'test/jenkins' || branch == 'origin/test/jenkins') {
                        echo '야호'
                    }
                }
            }
        }

        stage('CI : checkout') {
            steps {
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

    } // end of stages
} // end of pipeline
