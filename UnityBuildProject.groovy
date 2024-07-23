pipeline{
    agent any

    enviroment{
        unity_version= '2020.2.1f1'
        unity_projectWin= 'C:\\Program Files\\Unity\\Hub\\Editor\\2022.2f1\\Editor\\Unity.exe'
        unity_BuildPath_Win = "G:\\My Drive\\Black Mind Studios\\Projects\\Project Monkey Buttz\\Build_${BUILD_NUMBER}"
        unity_BuildPath_lin = ""
        unity_Executable_Win = "C:\\Program Files\\Unity\\Hub\\Editor\\${unity_version}/Editor/Unity"
        unity_Executable_Lin = ""
    }

    parameters{
        choice(name: 'project', choices: ['ProjectMonkeyButtzCode', 'LaserBirdsMK2'], description: 'Select the branch to build')
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
        stage("SCM checkout"){
            steps{
                checkout scmGit(branches: [[name: '*/${branch}']], extensions: [], userRemoteConfigs: [[credentialsId: 'GitUserAccount', url: 'https://github.com/JeffreyRock/${Project}']])
            }
        }
        stage("Build unity game"){
            steps{
                powershell """
                    \unityExecutable = '${unity_Executable_Win}'
                    \$projectPath = '.\\ProjectMonkeyButtzCode\\'
                    \$buildPath = '${unity_BuildPath_Win}'
                    \$BuildNumber = '${BUILD_NUMBER}'
                    & \$unityExecutable -batchmode -nographics -quit -projectPath \$projectPath -executeMethod BuildScript.Build -logFile build_\${buildNumber}.log -buildOutput \$buildPath -buildNumber \$buildNumber
                """
            }
        }
        stage("Move Project to archive"){
            steps{
                powershell
                """
                cd ${workspace}
                mkdir E:\\engine\\${env.BUILD_NUMBER}
                xcopy /E /I /Y build E:\\engine\\${env.BUILD_NUMBER}
                rmdir /S /Q build
                """
            }
        }
        stage("Discord Bot \"buildy da build bot\" response"){
            steps{
                echo "fix in the future"
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