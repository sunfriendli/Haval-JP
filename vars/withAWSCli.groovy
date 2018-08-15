#!/usr/bin/env groovy

def call(String param, Closure body) {

    sh 'pip3 install awscli --user'
    def command = "PATH=$PATH:$HOME/.local/bin;" + param
    println command
    if (body == null) {
        sh command
    } else {
        body.call(command)
    }

    //println test
    //withEnv(["PATH+AWSCLI=/home/lisi/.local/bin"], body)
}
