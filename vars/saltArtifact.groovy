#!/usr/bin/env groovy

import groovy.json.JsonOutput

def copyArtifactFromJob(job, build, glob) {
    copyArtifacts projectName: job, selector: specific(build), filter: glob
}

def getJenkinsUrl() {
    def jenkins = env.JENKINS_URL
    if (jenkins.endsWith("/")) {
        jenkins = jenkins.substring(0, jenkins.length() - 1);
    }
    return jenkins
}

def getJenkinsArtifactUrl(job, build, glob) {
    def files = findFiles(glob: glob)
    assert files.size() > 0
    def path = files[0].path
    path = path.replace("${env.WORKSPACE}/", '')

    job = job.replace('/', '/job/')
    def jenkins = getJenkinsUrl()
    return "${jenkins}/job/${job}/${build}/artifact/${path}"
}

def uploadArtifactToS3(job, build, glob, targetEnv) {
    def s3Uri = s3UploadEx(glob: glob,
        prefix: "${job}/${build}",
        credential: targetEnv['aws.credential'],
        region: targetEnv['aws.region'],
        bucket: targetEnv['aws.s3.bucket.server'],
    )
    return s3Uri
}

// Manifest

def getManifestFileName(name) {
    return "${name}.json"
}

def getManifest(name, version, artifact) {
    def m = [
        'name': name,
        'version': version,
        'artifact': artifact
    ]
    def json = JsonOutput.toJson(m)
    return JsonOutput.prettyPrint(json)
}

def getManifestS3Prefix(envName) {
    return "manifest/${envName}"
}

def updateManifest(name, version, artifact, envName, targetEnv) {
    def fileName = getManifestFileName(name)
    def manifest = getManifest(name, version, artifact)
    def s3Prefix = getManifestS3Prefix(envName)

    s3UploadText(
        credential: targetEnv['aws.credential'],
        region: targetEnv['aws.region'],
        bucket: targetEnv['aws.s3.bucket.server'],
        prefix: s3Prefix,
        text: manifest,
        file: fileName,
        contentType: 'application/json;charset=utf-8',
        acl: 'Private'
    )
}

def call(Map params = [:]) {
    def targetEnvName = params.get('env', '')
    def job = params.get('job', '')
    def build = params.get('build', '')
    def glob = params.get('glob', '')
    def name = params.get('name')
    assert targetEnvName
    assert job
    assert build
    assert glob
    assert name

    def targetEnv = loadEnv(targetEnvName)
    assert targetEnv

    if (env.JOB_NAME != job) {
        copyArtifactFromJob(job, build, glob)
    }

    def artifact = null
    if (targetEnv['salt.artifact.src'] == 'http') {
        artifact = getJenkinsArtifactUrl(job, build, glob)
    } else {
        artifact = uploadArtifactToS3(job, build, glob, targetEnv)
        updateManifest(name, build, artifact, targetEnvName, targetEnv)
    }
    return artifact
}
