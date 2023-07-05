pipeline {
    agent{
        node {
            label 'java17'
        }
    }
    options { skipStagesAfterUnstable() }
    environment {
        AWS_ACCESS_KEY_ID     = credentials('jenkins-aws-secret-key-id')
        AWS_SECRET_ACCESS_KEY = credentials('jenkins-aws-secret-access-key')
        DOCKER_AUTH_CREDENTIALS = credentials('dockerHub-login-pass')
    }
    stages {
     stage('Cleaning') {
       steps {
         // Clean before build
         cleanWs()
           // We need to explicitly checkout from SCM here
           checkout scm
           echo "Building ${env.JOB_NAME}..."
        }
     }
         
    stage('Build') {
      steps {
        //gradle build
        sh 'chmod +x gradlew'
        sh "./gradlew assemble -PbuildNumber=${env.BUILD_ID} -PbranchName=${env.BRANCH_NAME}"
      }
    }
    
    stage('Building image') {
      steps {
        //building a docker image 
        
        sh "docker build --network=host -t socbackend:${env.BRANCH_NAME}-${env.BUILD_ID} ."

        // tagging the image
        
        sh "docker tag socbackend:${env.BRANCH_NAME}-${env.BUILD_ID} 056740706980.dkr.ecr.ap-south-1.amazonaws.com/soc-backend:${env.BRANCH_NAME}-${env.BUILD_ID}"
        }
      }
    
    stage('Upload Image') {
      steps {
          //push the image to ecr registry
            script{
              docker.withRegistry('https://056740706980.dkr.ecr.ap-south-1.amazonaws.com', 'ecr:ap-south-1:056740706980'){ 
              sh "docker push 056740706980.dkr.ecr.ap-south-1.amazonaws.com/soc-backend:${env.BRANCH_NAME}-${env.BUILD_ID}"
              }
            }
          }
       }
     }
  }

