def call(dockerRepoName, imageName, portNum) {
    pipeline {
        agent any

        parameters {
            booleanParam(defaultValue: false, description: 'Deploy the App', name: 'DEPLOY') 
        }

        stages {
            stage('Build') {
                steps {
                    sh "rm -rf venv || true"
                    sh '''
                        python3 -m venv ./venv
                        . venv/bin/activate
                        ./venv/bin/pip install -r requirements.txt 
                        ./venv/bin/pip install --upgrade flask 
                        ./venv/bin/pip install xmlrunner
                        ./venv/bin/pip install coverage
                    '''
                }
            }
            stage('Python Lint') {
                steps {
                    sh 'pylint --fail-under 5 *.py'
                }
            }
            stage('Test and Coverage') {
                steps {
                    script {
                        // Remove existing test reports if they exist
                        def test_reports_exist = fileExists 'test-reports'
                        if (test_reports_exist) { 
                            sh 'rm test-reports/*.xml || true'
                        }
                        def api_test_reports_exist = fileExists 'api-test-reports'
                        if (api_test_reports_exist) { 
                            sh 'rm api-test-reports/*.xml || true'
                        }
                        
                        // Run unit tests and collect coverage data
                        def files = findFiles(glob: "test*.py")
                        for (file in files) {
                            sh "./venv/bin/coverage run --omit */dist-packages/*,*/site-packages/* $file"
                            sh "./venv/bin/coverage report"
                            sh "./venv/bin/coverage xml -o coverage.xml"
                        }
                    }
                }
                post { 
                    always { 
                        // Process test results
                        script {
                            def test_reports_exist = fileExists 'test-reports'
                            if (test_reports_exist) { 
                                junit 'test-reports/*.xml'
                            }
                            def api_test_reports_exist = fileExists 'api-test-reports'
                            if (api_test_reports_exist) { 
                                junit 'api-test-reports/*.xml'
                            }
                        }
                    }
                }
            }
            stage('Package') {
                when {
                    expression { env.GIT_BRANCH == 'origin/main' }
                }
                steps {
                    withCredentials([string(credentialsId: 'DockerHub', variable: 'TOKEN')]) {
                        sh "docker login -u 'sfreemanpo98' -p '$TOKEN' docker.io"
                        sh "docker build -t ${dockerRepoName}:latest --tag sfreemanpo98/${dockerRepoName}:${imageName} ."
                        sh "docker push sfreemanpo98/${dockerRepoName}:${imageName}"
                    }
                }
            }
            stage('Zip Artifacts') {
                steps {
                    sh 'zip -r app.zip *.py' 
                    archiveArtifacts artifacts: 'app.zip'
                }
            }
            stage ('Deliver') {
                when {
                    expression { params.DEPLOY }
                }
                steps {
                    sh "docker stop ${dockerRepoName} || true"
                    sh "docker rm ${dockerRepoName} || true"
                    sh "docker run -d -p ${portNum}:${portNum} --name ${dockerRepoName} ${dockerRepoName}:latest"
                }
            }

        }
    }
}
