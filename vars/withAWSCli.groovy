#!/usr/bin/env groovy

def call(String cmd1, Closure body) {
    def cmd = "PATH=$PATH:/home/lisi/.local/bin;" + cmd1
    println cmd
    body.call(cmd)
}
