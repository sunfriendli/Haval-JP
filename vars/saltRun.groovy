#!/usr/bin/env groovy

import groovy.json.JsonOutput

def call(Map params = [:]) {
    def envName = params.get('env', '')
    assert envName
    def mods = params.get('mods', '')
    assert mods

    def env = loadEnv(envName)
    assert env

    def output = salt(credentialsId: env['salt.credential'],
        servername: env['salt.endpoint'],
        authtype: 'pam',
        clientInterface: runner(function: 'state.sls',
            mods: mods,
            arguments: '',
            pillarvalue: ''
        )
    )

    echo JsonOutput.prettyPrint(output)
}
