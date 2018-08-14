#!/usr/bin/env groovy

// SeasunPak is configured via Custom Tools Plugin: https://plugins.jenkins.io/custom-tools-plugin
// Manage Jenkins -> Global Tool Configuration
def call(String version, Closure body) {
    def seasunPakHome = tool name: "SeasunPak-${version}", type: 'com.cloudbees.jenkins.plugins.customtools.CustomTool'
    withEnv(["PATH+SEASUNPAK=${seasunPakHome}"], body)
}
