#!/usr/bin/env groovy

def call() {
    def logFileName = "UAT_Log.txt"
    if (isUnix()) {
        sh "cp -vf \"${env.HOME}/Library/Logs/Unreal Engine/LocalBuildLogs/${logFileName}\" ${env.WORKSPACE}/${logFileName}"
    } else {
        bat "xcopy /y \"${env.HOME}\\Library\\Logs\\Unreal Engine\\LocalBuildLogs\\${logFileName}\" ${env.WORKSPACE}"
    }
    
    archiveArtifacts artifacts: logFileName, onlyIfSuccessful: false, allowEmptyArchive: true

    if (isUnix()) {
        sh "rm -vf ${logFileName}"
    } else {
        bat "del /q ${logFileName}"
    }
}
