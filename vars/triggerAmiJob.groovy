#!/usr/bin/env groovy

def getAmiName(def job) {
    def jobName = getJobName(job)
    return "ubuntu/18.04/${jobName}/${env.BUILD_DATE}-b${env.BUILD_NUMBER}"
}

def getJobName(def job) {
    return job.substring(job.lastIndexOf("/") + 1, job.length())
}

def getJobResultFileName(def job) {
    return getJobName(job) + "-ami.txt"
}

def getAmiFilePath(def job) {
    return "${JENKINS_HOME}/workspace/" + job + "/aws-ami/" + getJobResultFileName(job)
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

def buildJob(def yaml, def dynamicParams) {
    yaml.each {
        k, v ->
        v = v + dynamicParams
        buildresult = build(job: k, wait: true, propagate: true, parameters: getBuildParameters(v))
        if (v.containsKey('children')) {
            dynamicParams['source_ami'] = readFile file: getAmiFilePath(k)
            dynamicParams['ami_name'] = getAmiName()
            buildJob(v['children'], dynamicParams)    
        }
    }
}

def call(Map m = [:]) {

    def file = m.get('file', '')
    def paramMap = m.get('paramMap', '')
        
    assert file
    assert paramMap
    
    def yaml = readYaml file: file
    assert yaml.size() == 1

    buildJob(yaml, paramMap)
}