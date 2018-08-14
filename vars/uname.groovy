#!/usr/bin/env groovy

def call() {
    if (isUnix()) {
        def os = sh(script: 'uname', returnStdout: true).trim()
        if (os == 'Linux') {
            return 'Linux'
        } else if (os == 'Darwin') {
            return 'Mac'
        } else {
            error "Unknown OS: ${os}"
        }
    } else {
        return 'Windows'
    }
}
