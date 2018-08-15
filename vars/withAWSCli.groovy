#!/usr/bin/env groovy

def call(String test, Closure body) {

    //def cmd = "PATH=$PATH:/home/lisi/.local/bin;" + cmd1
    //println cmd
    //body.call(cmd)

    println test
    sh 'pip3 install awscli --user'
    withEnv(["PATH+AWSCLI=/home/lisi/.local/bin"], body)
}
