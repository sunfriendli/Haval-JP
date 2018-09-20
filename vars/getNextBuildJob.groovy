#!/usr/bin/env groovy

def call(String fileName, String job, List parents) {

    def yaml = readYaml file: fileName
    yaml = yaml[parents.remove(0)]
    parents.each { yaml = yaml['children'][it] }
    yaml = yaml[job]
    //yaml.remove('children')
    println yaml
}
