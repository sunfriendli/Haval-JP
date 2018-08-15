#!/usr/bin/env groovy

def call(String param, boolean needResult, Closure body) {

    sh 'pip3 install awscli --user'
    def command = "PATH=$PATH:$HOME/.local/bin;" + param
    println command
    if (needResult) {
        body.call(command)
    } else {
        sh command
    }

    //println test
    //withEnv(["PATH+AWSCLI=/home/lisi/.local/bin"], body)
}
