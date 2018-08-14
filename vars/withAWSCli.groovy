#!/usr/bin/env groovy

def call(Closure body) {
    sh 'pip3 install awscli --user'
    sh 'export PATH=~/.local/bin:$PATH;'
}
