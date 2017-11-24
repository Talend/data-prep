pipeline {
  agent any
  stages {
    stage('checkout') {
      steps {
        git(url: 'https://github.com/Talend/data-prep.git', branch: 'master')
      }
    }
  }
}