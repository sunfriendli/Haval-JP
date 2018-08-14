#!/usr/bin/env groovy

import groovy.json.JsonOutput

def call(Map params = [:]) {
    def envName = params.get('env')
    assert envName
    def pillar = params.get('pillar')
    def sls = params.get('sls')
    assert sls
    def grain = params.get('grain')
    assert grain

    def arguments = "${sls} queue=True"
    if (pillar) {
        def pillarJson = JsonOutput.toJson(pillar)
        arguments = "${arguments} \"pillar=${pillarJson}\""
    }

    echo "[saltState] arguments - ${arguments}"

    def env = loadEnv(envName)
    salt(credentialsId: env['salt.credential'],
        servername: env['salt.endpoint'],
        authtype: 'pam',
        clientInterface: local(function: 'state.sls',
            arguments: arguments,
            target: grain, targettype: 'grain',
            blockbuild: true, jobPollTime: 5, minionTimeout: 60
        )
    )
}
