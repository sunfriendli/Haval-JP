#!/usr/bin/env groovy

def call(String name, Closure body) {
    def cmakeHome = tool name: name, type: 'hudson.plugins.cmake.CmakeTool'
    withEnv(["PATH+CMAKE=${cmakeHome}/bin"], body)
}
