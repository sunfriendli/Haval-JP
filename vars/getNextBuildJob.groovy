#!/usr/bin/env groovy

def call(String fileName, List job) {

    def yaml = readYaml file: fileName
    if (job != null && job.size() != 0) {
        yaml = yaml[jobChain.remove(0)]
    }
    job.each { yaml = yaml['children'][it] }

    def parameters = []
    yaml.each {
        k, v -> 
        if (k != 'children') {
            parameters.add("string(name: ${k}, value: ${v}),")
        }
        
    }
    println parameters
}
