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
        stage("Fresh build Monkey"){
            when {
                expression{params.cleanWorkspace == true} 
            }
            steps{
                cleanWs()
            }
        }
        stage("SCM checkout"){
            steps{
                checkout scmGit(branches: [[name: '${branch}']], extensions: [], userRemoteConfigs: [[credentialsId: 'GitUserAccount', url: "https://github.com/JeffreyRock/${project}"]])
            }
        }
        stage("build laserBirds  "){
            when {
                expression{params.project == 'LaserBirdsMK2'}
            }
            steps{
                bat"""
                    move ${workspace}\\LazerBirdsMK2\\* ${workspace}\\
                    move ${workspace}\\LazerBirdsMK2\\Assets ${workspace}\\Assets
                    move ${workspace}\\LazerBirdsMK2\\Packages ${workspace}\\Packages
                    move ${workspace}\\LazerBirdsMK2\\ProjectSettings ${workspace}\\ProjectSettings
                    move ${workspace}\\LazerBirdsMK2\\Library ${workspace}\\Library
                    move ${workspace}\\LazerBirdsMK2\\Logs ${workspace}\\Logs
                    move ${workspace}\\LazerBirdsMK2\\obj ${workspace}\\obj
                    move ${workspace}\\LazerBirdsMK2\\UserSettings ${workspace}\\UserSettings
                """
            }
        }
        stage("Build unity project") {
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
        stage("Move Project to Project Monkey") {
            when {
                expression{params.project == 'ProjectMonkeyButtzCode'}
            }
            steps {
                bat """
                cd ${workspace}
                mkdir "G:\\My Drive\\Black Mind Studios\\Projects\\Project Monkey Buttz\\JenkinBuilds\\Build_${BUILD_NUMBER}"
                xcopy /E /I /Y Builds "G:\\My Drive\\Black Mind Studios\\Projects\\Project Monkey Buttz\\JenkinBuilds\\Build_${BUILD_NUMBER}"
                rmdir /S /Q Builds
                """
            }
        }
        stage("Move Project to Laser Birds") {
            when {
                expression{params.project == 'LaserBirdsMK2'}
            }
            steps {
                bat """
                cd ${workspace}
                mkdir "G:\\My Drive\\Black Mind Studios\\Projects\\Lazer Birds\\Builds\\Build_${BUILD_NUMBER}"
                xcopy /E /I /Y Builds "G:\\My Drive\\Black Mind Studios\\Projects\\Lazer Birds\\Builds\\Build_${BUILD_NUMBER}"
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