pipeline{
    agent any

    environment {
        unity_version= '2020.2.1f1'
        unity_projectWin= '"C:\\Program Files\\Unity\\Hub\\Editor\\2020.2.1f1\\Editor\\Unity.exe"'
        unity_BuildPath_Win = '"G:\\My Drive\\Black Mind Studios\\Projects\\Project Monkey Buttz\\Build_${BUILD_NUMBER}"'
        unity_BuildPath_lin = ""
        unity_Executable_Win = '"C:\\Program Files\\Unity\\Hub\\Editor\\2020.2.1f1\\Editor\\Unity.exe"'
        unity_Executable_Lin = ""
    }

    parameters{
        choice(name: 'project', choices: ['ProjectMonkeyButtzCode', 'LaserBirdsMK2'], description: 'Select the branch to build')
        choice(name: "branch", choices:['master','main'])
        booleanParam(name: 'cleanWorkspace', defaultValue: false, description: 'start fresh')
    }
    stages {
        stage("Fresh build"){
            when {
                expression{params.cleanWorkspace == true} 
            }
            steps{
                cleanWs()
            }
        }
        stage("SCM checkout"){
            steps{
                checkout scmGit(branches: [[name: '${branch}']], extensions: [], userRemoteConfigs: [[credentialsId: 'GitUserAccount', url: 'https://github.com/JeffreyRock/ProjectMonkeyButtzCode']])
            }
        }
        stage("Build unity game") {
            steps {
                bat """
                    set unityExecutable=${unity_Executable_Win}
                    set projectPath= ${workspace}\\
                    set buildPath=${unity_BuildPath_Win}
                    set buildNumber=${BUILD_NUMBER}
                    
                    echo Unity Executable: %unityExecutable%
                    echo Project Path: %projectPath%
                    echo Build Path: %buildPath%
                     %unityExecutable% -batchmode -nographics -quit -projectPath %projectPath% -executeMethod BuildScript.BuildWindows -logFile Builds\\build_%buildNumber%.log -buildOutput Builds -buildNumber %buildNumber%
                """
            }
        }
        stage("Move Project to archive") {
            steps {
                bat """
                cd ${workspace}
                mkdir "G:\\My Drive\\Black Mind Studios\\Projects\\Project Monkey Buttz\\Build_${BUILD_NUMBER}"
                xcopy /E /I /Y Builds "G:\\My Drive\\Black Mind Studios\\Projects\\Project Monkey Buttz\\Build_${BUILD_NUMBER}"
                rmdir /S /Q Builds
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