#!/usr/bin/env groovy

def call(String fileName, String job, List parents) {

    def yaml = readYaml file: fileName
    parents.each { yaml = yaml[it]['children'] }
    yaml = yaml[job]
    yaml.remove('children')
    println yaml
}
