def call() {
    pipeline {
        agent any
        stages {
            stage('Build') {
                steps {
                    sh '''
                        python3 -m venv ~/venv
                        ~/venv/bin/pip install -r requirements.txt --break-system-packages
                        ~/venv/bin/pip install --upgrade flask --break-system-packages
                        ~/venv/bin/pip install xmlrunner
                        ~/venv/bin/pip install coverage
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
                            sh "~/venv/bin/coverage run --omit */dist-packages/*,*/site-packages/* $file"
                            sh "~/venv/bin/coverage report"
                            sh "~/venv/bin/coverage xml -o coverage.xml"
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
            stage('Zip Artifacts') {
                steps {
                    sh 'zip -r app.zip *.py' 
                    archiveArtifacts artifacts: 'app.zip'
                }
            }
        }
    }
}
