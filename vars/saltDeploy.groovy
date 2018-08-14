#!/usr/bin/env groovy

import groovy.json.JsonOutput

def call(Map params = [:]) {
    def envName = params.get('env')
    assert envName
    def artifact = params.get('artifact')
    assert artifact
    assert params.get('sls')
    assert params.get('grain')

    def env = loadEnv(envName)
    def pillar = [
        'src': env['salt.artifact.src'],
        'artifact': artifact
    ]
    if (pillar['src'] == 's3') {
        pillar['region'] = env['salt.artifact.region']
    }

    params['pillar'] = pillar

    saltState params
}
