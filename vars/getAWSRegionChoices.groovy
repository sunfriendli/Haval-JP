#!/usr/bin/env groovy

def call() {
    def choices = [
        'cn-north-1',
    ]
    return choices.join('\n')
}
