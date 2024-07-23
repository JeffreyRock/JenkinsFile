pipeline {
    agent any

    environment {
        Build_Dir = 'build'
        MSBuild_Path = "Engine\\ConsoleApplication1\\ConsoleApplication1"
        GLFW_DIR = 'C:\\Jenkins\\workspace\\TestJob\\Engine\\ConsoleApplication1\\ConsoleApplication1\\GLFW\\glfw-3.4.bin.WIN64'
        
        Warning = false
        Failed = false
    }
    parameters{
         choice(name: 'branch', choices: ['main', 'develop', 'feature-1'], description: 'Select the branch to build')
         booleanParam(name: 'cleanWorkspace', defaultValue: false, description: 'start fresh')
    }
    stages {
        stage("Fresh build"){
            when {
                expression{params.cleanWorkspace == true && env.NODE_LABELS.contains('Windows')} 
            }
            steps{
                bat"""
                rmdir /S /Q ${workspace}
                """
            }
        }
        stage("Git Checkout") {
            steps {
                checkout scmGit(branches: [[name: '*/${branch}']], extensions: [], userRemoteConfigs: [[credentialsId: 'GitUserAccount', url: 'https://github.com/JeffreyRock/OpenGLEngine.git']])
            }
        }
        stage("Build Bash") {
            when {
                expression { env.NODE_LABELS.contains('Bash') }
            }
            steps {
                sh """
                    mkdir -p $Build_Dir
                    cd ./Engine/ConsoleApplication1/ConsoleApplication1
                    g++ -I ./GLFW/glfw-3.4.bin.WIN64/include -I ./GLFW/glfw-3.4.bin.WIN64/include/glad -L ./GLFW/glfw-3.4.bin.WIN64/lib-mingw-w64 -o ../../../../build/engine ConsoleApplication1.cpp glad.c ./GLFW/glfw-3.4.bin.WIN64/include/glad/glad.h shader.h shader.cpp -lglfw3 -lopengl32 -lgdi32
                """
            }
        }
        stage("Build Windows") {
            when {
                expression { env.NODE_LABELS.contains('Windows') }
            }
            steps {
                bat """
                    mkdir %Build_Dir%
                    cd .\\Engine\\ConsoleApplication1\\ConsoleApplication1
                    g++ -I .\\GLFW\\glfw-3.4.bin.WIN64\\include -I .\\GLFW\\glfw-3.4.bin.WIN64\\include\\glad -L .\\GLFW\\glfw-3.4.bin.WIN64\\lib-mingw-w64 -o ..\\..\\..\\build\\engine ConsoleApplication1.cpp glad.c .\\GLFW\\glfw-3.4.bin.WIN64\\include\\glad\\glad.h shader.h shader.cpp -lglfw3 -lopengl32 -lgdi32"
                """
            }
        }
        stage("Test Mac/Linux") {
            when {
                expression { env.NODE_LABELS.contains('Bash') }
            }
            steps {
                sh"""
                    cd ${workspace}/build
                    ./engine.exe 
                """
            }
        }
        stage("Test Windows") {
            when {
                expression { env.NODE_LABELS.contains('Windows') }
            }
            steps {
                bat"""
                    cp ${workspace}\\Engine\\ConsoleApplication1\\ConsoleApplication1\\Shaders ${workspace}\\build
                    cd ${workspace}/build
                    start engine.exe
                    ping 127.0.0.1 -n 10 > nul
                    taskkill /im engine.exe /f
                """
            }
        }
        stage("Archive") {
            steps {
                bat """
                cd ${workspace}
                mkdir E:\\engine\\${env.BUILD_NUMBER}
                xcopy /E /I /Y build E:\\engine\\${env.BUILD_NUMBER}
                rmdir /S /Q build
                """
            }
        }
    }

    post {
        always {
            echo "Pipeline execution finished"
            cleanWs cleanWhenAborted: false, cleanWhenSuccess: false, cleanWhenUnstable: false, patterns: [[pattern: '', type: 'INCLUDE']]
        }
    }
}
