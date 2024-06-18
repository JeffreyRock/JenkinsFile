pipeline {
    agent any

    enviroment {
        Build_Dir ='build'
        MSBuild_Path "C:\\"
        Warning = False
        Failed = False
    }
    parameters {
        
    }
    stages{
        stage ("Git Checkout"){
            Git 
        }
        stage("Build Bash"){
            when{
                expression(env.NODE_LABELS.contains('Bash'))
            }
            steps{

            }
        }
        Stage ("Build WIndows "){
            when{
                expression(env.NODE_LABELS.contains('Windows'))
            }
            steps{
                bat """
                    mkdir %Build_Dir%
                    CMAKE --build -S . -B %Build_Dir%
                """
            }
        }
        Stage ("Test")
        {

        }
        stage ("Archive"){

        }
        post{
            
        }
    }
}