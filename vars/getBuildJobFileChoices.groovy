#!/usr/bin/env groovy

// Return job names that can be used to pack AMI.

def call() {
    def choices = [
        'config.yml'
    ]
    return choices.join('\n')
}
