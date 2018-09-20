#!/usr/bin/env groovy

def call(String fileName, List job) {

    def yaml = readYaml file: fileName
    if (job != null && job.size() > 1) {
        yaml = yaml[job.remove(0)]
        job.each { yaml = yaml['children'][it] }
    }

    def parameters = []
    yaml.each {
        k, v -> 
        if (k != 'children') {
            parameters.add("string(name: ${k}, value: ${v}),")
        }
        
    }
    println parameters
}
