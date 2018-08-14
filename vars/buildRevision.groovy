#!/usr/bin/env groovy

import groovy.json.JsonOutput

def getRevisionFile() {
    return 'revision.json'
}

def getCopyArtifactTargetDir() {
    return 'lastSuccessfulBuild'
}

def save(Map m = [:]) {
    def engineRevision = m.get('engine')
    def gameRevision = m.get('game')

    assert engineRevision
    assert gameRevision

    def map = [
        'job': env.JOB_NAME,
        'build': env.BUILD_NUMBER,
        'engine': engineRevision,
        'game': gameRevision,
    ]
    def json = JsonOutput.toJson(map)
    json = JsonOutput.prettyPrint(json)
    def revisionFile = m.get('file', getRevisionFile())
    writeFile encoding: 'UTF-8', file: revisionFile, text: json
    archiveArtifacts artifacts: revisionFile, onlyIfSuccessful: true, allowEmptyArchive: false
}

def loadLastSuccessful() {
    def revisionFile = getRevisionFile()
    def targetDir = getCopyArtifactTargetDir()
    try {
        copyArtifacts filter: revisionFile, target: targetDir, projectName: env.JOB_NAME, selector: lastSuccessful()
    }
    catch (ex) {
        echo "Last successful build does not exist - ${ex.message}"
        return null
    }
    return readJSON(file: "${targetDir}/${revisionFile}")
}
