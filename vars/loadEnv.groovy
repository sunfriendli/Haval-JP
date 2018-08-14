#!/usr/bin/env groovy

//
// Load environment settings from resources as a Map.
//

import groovy.json.JsonOutput

def loadProperties(String name, defaults = null) {
    def resource = libraryResource "env/${name}.properties"
    return readProperties(text: resource, defaults: defaults)
}

def printMap(String name, map) {
    def json = JsonOutput.toJson(map)
    json = JsonOutput.prettyPrint(json)
    echo "[loadEnv] ${name}\n${json}"
}

def getDefaults() {
    return loadProperties("_default")
}

def call(String name, boolean debug = false) {
    def defaults = getDefaults()
    def map = loadProperties(name, defaults)
    if (debug) {
        printMap(name, map)
    }
    return map
}
