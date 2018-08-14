#!/usr/bin/env groovy

def call() {
    def choices = [
        'Asia/Shanghai',
        'America/Los_Angeles'
    ]
    return choices.join('\n')
}
