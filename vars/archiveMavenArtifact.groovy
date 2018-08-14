#!/usr/bin/env groovy

def call(Map params = [:]) {
    def glob = params.get('glob', '')
    def name = params.get('name', '')
    def extension = params.get('extension', '')
    assert name
    assert glob
    assert extension

    def files = findFiles(glob: glob)
    assert files.size() > 0
    def finalName = artifactName name: name, extension: extension
    sh "cp -vf ${files[0].path} ${env.WORKSPACE}/${finalName}"
    archiveArtifacts artifacts: finalName, onlyIfSuccessful: true
}
