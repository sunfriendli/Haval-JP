#!/usr/bin/env groovy

// Return job names that can be used to pack AMI.

def call() {
    def choices = [
        '\'base\'',
        '\'base\',\'saltmaster\'',
        '\'base\',\'filebeat\'',
        '\'base\',\'saltmaster\',\'test1\'',
        '\'base\',\'saltmaster\',\'test2\'',
    ]
    return choices.join('\n')
}
