#!/usr/bin/env groovy

//
// Return environment names that can be used in a choices parameter.
//

def call() {
    def choices = [
        'hydra-a1',
        'hydra-b1',
        'hub',
        'dms',
        'dungeon',
        'elk'
    ]
    return choices.join('\n')
}
