#!/usr/bin/env groovy

def call(String name, Closure body) {
    //sh 'pip3 install awscli --user'
    //sh 'export PATH=~/.local/bin:$PATH;cmd'
    //sourceAmiId = sh(script: "${findLatestAmi}", returnStdout: true).trim()
    //println(name)
    //echo "awscli groovy"
    //println(this.region)
    //this.sourceAmiId = "test"
    sh 'pip3 install awscli --user'
    sh 'find ~/.local/bin -name aws'
    withEnv(['MYTOOL_HOME=/home/sunfriendli/.local/bin']) {
        sh '$MYTOOL_HOME/.aws --version'
    }
}
