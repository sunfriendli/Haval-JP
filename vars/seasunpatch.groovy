#!/usr/bin/env groovy

def getAppId(Map m = [:]) {
    def env = m.get('env')
    assert env
    def platform = m.get('platform')
    assert platform
    // This ID is stored in database, DO NOT CHANGE THIS!
    return "pirates.${env}.${platform}"
}

def getPatchFileName(Map m = [:]) {
    def baseResVersion = m.get('baseResVersion')
    assert baseResVersion
    def resVersion = m.get('resVersion')
    assert resVersion
    return "pirates-${baseResVersion}-to-${resVersion}.patch.pak"
}

def getPatchS3Prefix(Map m = [:]) {
    def env = m.get('env')
    assert env
    def platform = m.get('platform')
    assert platform
    def appVersion = m.get('appVersion')
    assert appVersion
    return "${env}/patch/${platform}/${appVersion}"
}

def getManifestS3Prefix(Map m = [:]) {
    def env = m.get('env')
    assert env
    def platform = m.get('platform')
    assert platform
    "${env}/patch/${platform}"
}

def s3UploadPatch(Map m = [:]) {
    def envName = m.get('env')
    assert envName
    def patchFile = m.get('patchFile')
    assert patchFile

    def targetEnv = loadEnv(envName)
    def s3Credential = targetEnv['aws.credential']
    def s3Region = targetEnv['aws.region']
    def s3Bucket = targetEnv['aws.s3.bucket.client']
    def s3Prefix = getPatchS3Prefix(m)

    return s3UploadEx(
        glob: patchFile,
        credential: s3Credential,
        region: s3Region,
        bucket: s3Bucket,
        prefix: s3Prefix,
        acl: 'PublicRead',
        returnDownloadUrl: true,
    )
}

def s3UploadManifest(Map m = [:]) {
    def envName = m.get('env')
    assert envName
    def manifest = m.get('manifest')
    assert manifest

    def targetEnv = loadEnv(envName)
    def s3Credential = targetEnv['aws.credential']
    def s3Region = targetEnv['aws.region']
    def s3Bucket = targetEnv['aws.s3.bucket.client']
    def s3Prefix = getManifestS3Prefix(m)

    s3UploadText(
        credential: s3Credential,
        region: s3Region,
        bucket: s3Bucket,
        prefix: s3Prefix,
        text: manifest,
        file: 'manifest.json',
        contentType: 'application/json',
        acl: 'PublicRead'
    )
}

def prepareAppFile(projectName, buildNumber, platform) {
    def glob = null
    if (platform == 'android') {
        glob = '*.apk'
    } else if (platform == 'ios') {
        glob = '*.ipa'
    } else {
        error "Unsupported platform: ${platform}"
    }
    copyArtifacts(
        projectName: projectName,
        selector: specific(buildNumber),
        filter: glob
    )
    def files = findFiles(glob: glob)
    assert files.size() > 0
    return files[0]
}

def loadConf() {
    def resource = libraryResource 'seasunpatch.conf'
    return readProperties(text: resource)
}

// conf should be return value of loadConf()
def config(conf) {
    sh 'pip3 install --user boto3'
    call(
        command: 'config',
        args: ['dynamodb_table', conf.dynamodbTable],
    )
    call(
        command: 'config',
        args: ['archive_dir', conf.archiveDir],
    )
}

def call(Map m = [:]) {
    def command = m.get('command')
    assert command
    def args = m.get('args', [])
    def kwargs = m.get('kwargs', [:])
    assert args.size() > 0 || kwargs.size() > 0
    def debug = m.get('debug', false)
    def returnStdout = m.get('returnStdout', false)
    def parseJson = m.get('parseJson', false)

    def script = "seasunpatch ${command}"
    args.each {
        arg -> script += " '${arg}'"
    }
    kwargs.each {
        k, v -> script += " --${k} '${v}'"
    }
    if (debug) {
        script += ' --debug'
    }

    if (returnStdout) {
        def stdout = sh(script: script, returnStdout: true).trim()
        def result = parseJson ? readJSON(text: stdout) : stdout
        return result
    } else {
        sh script
    }
}
