#!/usr/bin/env groovy

import groovy.json.JsonOutput

def call(Map m = [:]) {
    def file = m.get('file', '')
    def object = m.get('object', '')
    def pretty = m.get('pretty', false)

    assert file
    assert object

    def json = JsonOutput.toJson(object)
    if (pretty) {
        json = JsonOutput.prettyPrint(json)
    }
    writeFile encoding: 'UTF-8', file: file, text: json
}
