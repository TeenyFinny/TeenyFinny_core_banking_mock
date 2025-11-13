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

        stage('checkout') {
            steps {
                cleanWs()
                sshagent(credentials: ['a70f1bb2-9116-4626-b231-c0bc7e17d257']) { // Jenkins Credentials ID
                    sh '''
                    GIT_SSH_COMMAND="ssh -o StrictHostKeyChecking=no" \
                    git clone --depth=1 --branch main git@github.com:JBL28/test.git .
                    '''
                }
            }
        }

    } // end of stages
} // end of pipeline
