#!/usr/bin/env groovy

def call(String cmd1, Closure body) {

    def cmd = "PATH=$PATH:$HOME/.local/bin;" + cmd1
    println cmd
    body.call(cmd)
}
