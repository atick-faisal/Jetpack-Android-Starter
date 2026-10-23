pipeline {
    agent { label 'android' }

    options {
        skipDefaultCheckout()
        disableConcurrentBuilds()
        timeout(time: 120, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle/ci-home"
    }

    stages {
        stage('Checkout') {
            steps {
                // Discard stale reports and local configuration before checking out this revision.
                deleteDir()
                checkout scm
            }
        }

        stage('Prepare environment') {
            steps {
                sh '''
                    set -eu
                    for tool in java git bash just python3.14; do
                        command -v "$tool"
                    done
                    java -version 2>&1 | head -n 1 | grep -E '\"21[.\"]'
                    export ANDROID_HOME="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
                    : "${ANDROID_HOME:?Configure ANDROID_HOME or ANDROID_SDK_ROOT on the android agent}"
                    test -d "$ANDROID_HOME/platforms"
                    mkdir -p "$GRADLE_USER_HOME"
                    cp .github/ci-gradle.properties "$GRADLE_USER_HOME/gradle.properties"
                    ./gradlew --version
                    python3.14 -m venv .venv
                    .venv/bin/python -m pip install mkdocs mkdocs-material mkdocs-github-admonitions-plugin==0.1.1 Pygments
                '''
            }
        }

        stage('Verify') {
            steps {
                sh 'just verify'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/build/test-results/**/*.xml,**/build/reports/lint-results-*',
                allowEmptyArchive: true
            junit testResults: '**/build/test-results/**/TEST-*.xml',
                allowEmptyResults: currentBuild.currentResult != 'SUCCESS',
                skipPublishingChecks: true
        }
        success {
            sh 'test -s app/build/outputs/apk/debug/app-debug.apk && test -s site/index.html'
            archiveArtifacts artifacts: 'app/build/outputs/apk/debug/app-debug.apk', fingerprint: true
            archiveArtifacts artifacts: 'site/**'
        }
    }
}
