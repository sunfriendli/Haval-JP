#!/usr/bin/env groovy

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
        v = dynamicParams == null ? v : v + dynamicParams
        println k
        buildresult = build(job: k, wait: true, propagate: true, parameters: getBuildParameters(v))
        dynamicParams['source_ami'] = readFile file: "${env.WORKSPACE}/${k}-ami.txt"
        if (v.containsKey('children')) {
            buildJob(v['children'], dynamicParams)    
        }
    }
}

def call(Map m = [:]) {

    def file = m.get('file', '')
    def sourceAmi = m.get('source_ami', '')
    def sourceAmiRelease = m.get('source_ami_release', '')
        
    assert file
    assert sourceAmi
    assert sourceAmiRelease
    
    def yaml = readYaml file: file
    assert yaml.size() == 1

    def dynamicParams = [:]
    dynamicParams['source_ami'] = sourceAmi
    dynamicParams['source_ami_release'] = sourceAmiRelease

    buildJob(yaml, dynamicParams)
}