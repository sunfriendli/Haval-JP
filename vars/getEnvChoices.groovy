#!/usr/bin/env groovy

//
// Return environment names that can be used in a choices parameter.
//

def call() {
    def choices = [
        'dev',
        'test',
        'prod',
        'banshu',
    ]
    return choices.join('\n')
}
