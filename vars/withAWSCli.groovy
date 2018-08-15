#!/usr/bin/env groovy

def call(Closure body) {
    //println "delegate:" + body.delegate.class.name
    //println "owner: " + body.owner.class.name

    def mapProperties = body.getProperties()
    println(mapProperties)
}
