#!/usr/bin/env groovy

def call(Closure body) {

    // sh 'pip3 install awscli --user'
    // def command = "PATH=$PATH:$HOME/.local/bin;" + param
    // println command
    // if (needResult) {
    //     body.call(command)
    // } else {
    //     sh command
    // }

    sh 'pip3 install --user boto3'
    //withEnv(['PATH+AWSCLI=~/.local/bin'], body)
    withEnv(['PATH+AWSCLI=/home/lisi/.local/bin'], body)
    
}
