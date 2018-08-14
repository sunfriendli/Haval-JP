#!/usr/bin/env groovy

def call(String name, Closure body) {
    def packerHome = tool name: name, type: 'biz.neustar.jenkins.plugins.packer.PackerInstallation'
    withEnv(["PATH+PACKER=${packerHome}"], body)
}
