pipeline {
    agent none

    options {
        timeout(time: 45, unit: 'MINUTES')
    }

    stages {
        stage('Generate & Deploy Docs') {
            agent {
                docker {
                    image 'thyrlian/android-sdk:latest'
                    args '-u root:root -v $HOME/.gradle:/root/.gradle'
                }
            }

            steps {
                script {
                    // Checkout
                    checkout scm

                    // Validate Gradle Wrapper
                    sh '''
                        chmod +x gradlew
                        ./gradlew --version
                    '''

                    // Copy CI gradle properties
                    sh '''
                        mkdir -p ~/.gradle
                        cp .github/ci-gradle.properties ~/.gradle/gradle.properties
                    '''

                    // Install Python and dependencies
                    sh '''
                        apt-get update
                        apt-get install -y python3 python3-pip python3-venv git
                        python3 -m venv venv
                        . venv/bin/activate
                        pip install mkdocs mkdocs-material mkdocs-github-admonitions-plugin Pygments
                    '''

                    // Generate Documentation
                    sh '''
                        ./gradlew dokkaGeneratePublicationHtml
                        mkdir -p docs/api
                        mv build/dokka/html/* docs/api/
                    '''

                    // Copy README
                    sh '''
                        cp README.md docs/index.md
                        sed -i 's/docs\\///' docs/index.md
                    '''

                    // Deploy to GitHub Pages with authentication
                    withCredentials([usernamePassword(credentialsId: 'github-pat', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')]) {
                        sh '''
                            . venv/bin/activate

                            # Configure git
                            git config --global user.email "jenkins@ci.local"
                            git config --global user.name "Jenkins CI"

                            # Set remote URL with authentication
                            git remote set-url origin https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/atick-faisal/Jetpack-Android-Starter.git

                            # Deploy
                            mkdocs gh-deploy --force
                        '''
                    }
                }
            }
        }
    }
}