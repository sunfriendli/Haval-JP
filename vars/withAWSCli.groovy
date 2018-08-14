#!/usr/bin/env groovy

def call(String cmd, String result, Closure body) {
    //sh 'pip3 install awscli --user'
    //sh 'export PATH=~/.local/bin:$PATH;cmd'
    //sourceAmiId = sh(script: "${findLatestAmi}", returnStdout: true).trim()
    println(this.region)
    this.sourceAmiId = "test"
}
