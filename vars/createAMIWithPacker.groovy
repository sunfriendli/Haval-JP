#!/usr/bin/env groovy

def getPackerVersion() {
    return '1.2.3-linux'
}

def getCheckoutUrl() {
    return 'https://github.com/rog2/aws-ami.git'
}

def getCheckoutBranch() {
    return 'master'
}

def getLocalCheckoutDir() {
    return 'aws-ami'
}

def loadConf() {
    def resource = libraryResource 'packer.conf'
    return readProperties(text: resource)
}

def getPackerCredential(Map conf, String region) {
    def key = "credential.${region}"
    return conf[key]
}

def getPackerSubnetId(Map conf, String region) {
    def key = "subnet-id.${region}"
    return conf[key]
}

// Use scripted pipeline here. 
// Note: packerVars key name should be consistent with variable name passed to packer.

def call(String template, List packerVars) {
    assert template

    def vars = []
    packerVars.each { entry ->
        vars.add(entry())
    }
    properties([
        buildDiscarder(logRotator(daysToKeepStr: '15')),
        disableConcurrentBuilds(),
        parameters(vars)
    ])

    node('os:linux') {
        ansiColor('xterm') {
            stage('Checkout') {
                checkout([$class: 'GitSCM',
                    userRemoteConfigs: [[url: getCheckoutUrl()]],
                    branches: [[name: getCheckoutBranch()]],
                    extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: getLocalCheckoutDir()]]
                ])
            }
            stage('Create AMI') {
                dir(getLocalCheckoutDir()) {
                    def region = params.get('region')
                    assert region

                    def packerBuildParams= ''
                    params.each {
                        k, v -> packerBuildParams = "${packerBuildParams} -var '${k}=${v}'"
                    }
                    packerBuildParams = "${packerBuildParams} ${template}"
                    echo "Packer build params: ${packerBuildParams}"

                    def conf = loadConf()
                    def packerCredential = getPackerCredential(conf, region)
                    def packerSubnetId = getPackerSubnetId(conf, region)

                    assert packerCredential
                    assert packerSubnetId

                    withAWS(credentials: packerCredential, region: region) {
                        withPacker(getPackerVersion()) {
                            sh "packer build ${packerBuildParams}"
                        }
                    }
                }
            }
        }
    }
}
