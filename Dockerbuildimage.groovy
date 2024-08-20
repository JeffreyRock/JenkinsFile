pipeline{
    agent {
      label 'Host'
    }
    environment {
        DOCKER_IMAGE = 'rockyeaglen7/discordbuildbot:latest'
    }
    parameters{
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
               checkout scmGit(branches: [[name: 'main']], extensions: [], userRemoteConfigs: [[credentialsId: 'GitUserAccount', url: "https://github.com/JeffreyRock/DiscordBuildNotificationBot"]])
            }
        }
        stage ("Change Docker files to include discord files"){
            steps{
                withCredentials([usernamePassword(credentialsId: 'DiscordToken', passwordVariable: 'discordPass', usernameVariable: 'discordUser')]) {
                    sh"""
                        sed -i 's/ENV DISCORD_TOKEN="your_discord_token_here"/ENV DISCORD_TOKEN="${discordUser}"/' DockerFIle.dockerfile
                        sed -i 's/ENV DISCORD_CHANNEL_ID="your_channel_id_here"/ENV DISCORD_CHANNEL_ID="${discordPass}"/' DockerFIle.dockerfile
                    """
                }
            }
        }
        stage ("Docker build"){
            steps{
                sh"""
                cd ${workspace}
                    docker build -t ${DOCKER_IMAGE} -f DockerFIle.dockerfile .
                    docker run -d -p 5000:5000 ${DOCKER_IMAGE}
                """
            }
        }
        stage("Docker push")
        {
            steps{
                withCredentials([usernamePassword(credentialsId: 'Docker', passwordVariable: 'DockerPass', usernameVariable: 'DockerUser')]) {
                    sh'''
                        docker login -u ${DockerUser} -p ${DockerPass}
                        docker push rockyeaglen7/discordbuildbot:latest
                    '''
                }
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