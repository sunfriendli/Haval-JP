#!/usr/bin/env groovy

def getJobName(def job) {
    return job.substring(job.lastIndexOf("/") + 1, job.length())
}

def getAmiName(def job) {
    def jobName = getJobName(job)
    return "ubuntu/18.04/${jobName}/${env.BUILD_DATE}-b${env.BUILD_NUMBER}"
}

def getBuildParameters(def yaml) {
    def parameters = []
    yaml.each {
        k, v -> 
        if (k != 'children') {
            parameters.add(string(name: k, value: v))
        }
    }
    return parameters
}

def buildJob(def yaml, def paramMap) {
    yaml.each {
        k, v ->
        paramMap = v == null ? paramMap : v + paramMap
        paramMap['ami_name'] = getAmiName(k)
        def buildresult = build(job: k, wait: true, propagate: true, parameters: getBuildParameters(paramMap))
        if (v?.containsKey('children')) {
            paramMap['source_ami'] = buildresult.getBuildVariables()['AMI_ID']
            buildJob(v['children'], paramMap)
        }
    }
}

// map: the parameters to build ami
// configDir: the location of yml configure, which stores relationship of jobs

def call(Map params, String configDir = 'config.yml') {

    assert params

    def yaml = readYaml file: configDir
    assert yaml

    buildJob(yaml, params)
}
