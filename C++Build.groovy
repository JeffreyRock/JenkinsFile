pipeline {
    agent any

    environment {
        Build_Dir = 'build'
        MSBuild_Path = "C:\\"
        Warning = false
        Failed = false
    }

    stages {
        stage("Git Checkout") {
            steps {
                git credentialsId: 'GitUserAccount', url: 'https://github.com/JeffreyRock/OpenGLEngine'
            }
        }
        stage("Build Bash") {
            when {
                expression { env.NODE_LABELS.contains('Bash') }
            }
            steps {
                sh """
                    mkdir -p $Build_Dir
                    cmake --build -S . -B $Build_Dir
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
                    g++ -I .\Engine\ConsoleApplication1\ConsoleApplication1\GLFW\glfw-3.4.bin.WIN64\include -I .\Engine\ConsoleApplication1\ConsoleApplication1\GLFW\glfw-3.4.bin.WIN64\include\glad -L .\Engine\ConsoleApplication1\ConsoleApplication1\GLFW\glfw-3.4.bin.WIN64\lib-mingw-w64 .\Engine\ConsoleApplication1\ConsoleApplication1\ConsoleApplication1.cpp .\Engine\ConsoleApplication1\ConsoleApplication1\GLFW\glfw-3.4.bin.WIN64\include\glad\glad.c -lglfw3 -lopengl32 -lgdi32 -o .\build\build.exe
                """
            }
        }
        stage("Test") {
            steps {
                echo "Test Complete"
            }
        }
        stage("Archive") {
            steps {
                echo "Archive Complete"
            }
        }
    }

    post {
        always {
            echo "Pipeline execution finished"
        }
    }
}
