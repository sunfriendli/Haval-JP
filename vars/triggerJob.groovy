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
        if (v.containsKey('children')) {
            buildJob(v['children'], dynamicParams)    
        }
    }
}

def call(Map m = [:]) {

    def file = m.get('file', '')
    def file = m.get('source_ami', '')
    def file = m.get('source_ami_release', '')
        
    assert file
    
    def yaml = readYaml file: file
    assert yaml.size() == 1

    buildJob(yaml, dynamicParams)
}