pipeline {
    agent any

    tools {
        jdk 'jdk17'
    }

    options {
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 30, unit: 'MINUTES')
    }

    triggers {
        pollSCM('H/2 * * * *')
    }

    environment {
        STUDENT_NAME = 'Ramazan'
        STUDENT_SURNAME = 'Brumkul'
        STUDENT_GROUP = 'IT2-2301'
        STUDENT_ID = '37911'
        APP_VERSION = '1.0'
        IMAGE_NAME = 'brumkul-ramazan-student-api'
        IMAGE_TAG = "v${APP_VERSION}-brumkul-ramazan"
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    def scmInfo = checkout scm
                    env.GIT_BRANCH = scmInfo.GIT_BRANCH
                    env.GIT_COMMIT = scmInfo.GIT_COMMIT
                }
                sh '''
                    echo "=================================================="
                    echo " Pipeline of ${STUDENT_NAME} ${STUDENT_SURNAME}"
                    echo " Group ${STUDENT_GROUP}, Student ID ${STUDENT_ID}"
                    echo " Image ${IMAGE_NAME}:${IMAGE_TAG}"
                    echo " Branch ${GIT_BRANCH}"
                    echo "=================================================="
                    git log -1 --pretty=format:'Commit %h by %an: %s%n'
                    java -version
                    docker version --format 'Docker client {{.Client.Version}}, server {{.Server.Version}}'
                '''
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew --no-daemon clean assemble -PappVersion=${APP_VERSION}'
                sh 'ls -lh build/libs/'
            }
        }

        stage('Test') {
            steps {
                sh './gradlew --no-daemon test -PappVersion=${APP_VERSION}'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'build/test-results/test/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    docker build \
                        --build-arg APP_VERSION=${APP_VERSION} \
                        --label jenkins.build=${BUILD_NUMBER} \
                        --label git.commit=${GIT_COMMIT} \
                        -t ${IMAGE_NAME}:${IMAGE_TAG} .
                '''
            }
        }

        stage('Verify') {
            steps {
                sh '''
                    docker image inspect ${IMAGE_NAME}:${IMAGE_TAG} > /dev/null
                    echo "Image ${IMAGE_NAME}:${IMAGE_TAG} exists"
                    docker image ls ${IMAGE_NAME}
                    docker image inspect --format 'Created: {{.Created}}' ${IMAGE_NAME}:${IMAGE_TAG}
                    docker image inspect --format 'Size: {{.Size}} bytes' ${IMAGE_NAME}:${IMAGE_TAG}
                    docker image inspect --format 'Owner label: {{index .Config.Labels "student.name"}}, ID {{index .Config.Labels "student.id"}}' ${IMAGE_NAME}:${IMAGE_TAG}
                    docker run --rm ${IMAGE_NAME}:${IMAGE_TAG} java -version
                    docker run --rm ${IMAGE_NAME}:${IMAGE_TAG} ls -l /app/app.jar
                    docker image prune -f --filter label=student.id=${STUDENT_ID} > /dev/null
                    echo "VERIFY OK: ${IMAGE_NAME}:${IMAGE_TAG} built for ${STUDENT_NAME} ${STUDENT_SURNAME}"
                '''
            }
        }
    }

    post {
        success {
            echo "SUCCESS: ${env.IMAGE_NAME}:${env.IMAGE_TAG} is built and verified. Student: ${env.STUDENT_NAME} ${env.STUDENT_SURNAME}"
        }
        failure {
            echo "FAILURE: the pipeline of ${env.STUDENT_NAME} ${env.STUDENT_SURNAME} stopped. Open the red stage and read its log."
        }
    }
}
