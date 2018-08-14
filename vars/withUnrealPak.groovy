#!/usr/bin/env groovy

// UnrealPak is configured via Custom Tools Plugin: https://plugins.jenkins.io/custom-tools-plugin
// Manage Jenkins -> Global Tool Configuration
def call(String version, Closure body) {
    def unrealPakHome = tool name: "UnrealPak-${version}", type: 'com.cloudbees.jenkins.plugins.customtools.CustomTool'
    withEnv(["PATH+UNREALPAK=${unrealPakHome}"], body)
}
