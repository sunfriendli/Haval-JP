#!/usr/bin/env groovy

def getBuildParameters(def yaml) {
    def parameters = []
    yaml.each {
        k, v -> 
        if (k != 'children') {
            parameters.add(string(name: ${k}, value: ${v}))
        }
    }
    return parameters
}

def buildJob(def name, def yaml) {
    yaml = yaml[name]
    build(job: name, wait: true, propagate: true, parameters: getBuildParameters(yaml))
    if (yaml.containkey('children')) {
        yaml = yaml['children']
        yaml.each {
            k, v ->
                buildJob(k, v) 
        }
    }
}

def call(Map m = [:]) {
    def root = m.get('root', '')
    def file = m.get('file', '')

    assert root
    assert file
    
    def yaml = readYaml file: file
    buildJob(root, yaml)
}