#!/usr/bin/env groovy

def call(String name, Closure body) {
    def goHome = tool name: name, type: 'go'
    withEnv(["PATH+GO=${goHome}/bin", "GOROOT=${goHome}"], body)
}
