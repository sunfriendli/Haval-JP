#!/usr/bin/env groovy

def getReadableDuration() {
    def duration = currentBuild.duration / 1000
    int hours = duration / 3600
    int minutes = duration / 60 - hours * 60
    int seconds = duration - minutes * 60 - hours * 3600
    if (hours > 0) {
        return "${hours} hr ${minutes} min"
    } else if (minutes > 0) {
        return "${minutes} min ${seconds} sec"
    } else {
        return "${seconds} sec"
    }
}

def getSummaryText() {
    def fileList = []
    def authorList = []
    currentBuild.changeSets.each {
        changeLogSet -> changeLogSet.items.each {
            entry -> authorList += entry.author.toString()
            entry.affectedFiles.each {
                file -> fileList += file.path.toString()
            }
        }
    }
    fileList.unique()
    authorList.unique()
    return "${fileList.size()} file(s) changed - ${authorList.join(',')}"
}

def getChangesText(changesMaxNum) {
    // Without summary, the changes could print more line
    def i = 1
    def msgList = []
    currentBuild.changeSets.each {
        changeLogSet -> changeLogSet.items.each {
            entry -> 
            if (i <= changesMaxNum) { 
                msgList += " - ${entry.msg} [${entry.author}]"
            }
            i++
        }
    }
    if (i <= changesMaxNum) {
        return msgList.join('\n')
    } else {
        msgList += "${i - changesMaxNum - 1} more... "
        return msgList.join('\n')
    }
}

def getBearychatMessage(what, duration) {
    return "[${env.JOB_NAME}](${env.JOB_URL}) - [#${env.BUILD_NUMBER}](${env.BUILD_URL}) ${what}${duration}"
}

def sendBearychat(channel, what, duration, withSummary, withChanges, attachmentText, color) {
    if (withSummary || withChanges) {
        bearychatSend(attachmentText: attachmentText,
            message: getBearychatMessage(what, duration),
            channel: channel, color: color)
    } else {
        bearychatSend message: getBearychatMessage(what, duration), channel: channel
    }
}

def call(Map params = [:]) {
    def channel = params.get('channel') // optional
    def what = params.get('what', '')
    assert what

    def withSummary = params.get('withSummary', false)
    def withChanges = params.get('withChanges', false)

    def summaryText = withSummary ? getSummaryText() : ''
    def changesText = withChanges ? getChangesText(withSummary ? 4 : 5) : ''

    def withDuration = params.get('withDuration', false)
    def duration = withDuration ? " after ${getReadableDuration()}" : ''

    def attachmentText = "${summaryText} \n${changesText}"

    // Prevent blank lines
    if (!withSummary && withChanges) {
        attachmentText = " ${summaryText}${changesText}"
    }
    if (currentBuild.changeSets.size() == 0) {
        attachmentText = "No changes"
    }

    def color = currentBuild.result == "FAILURE" ? "red" : "green"
    
    sendBearychat(channel, what, duration, withSummary, withChanges, attachmentText, color)
}

// what can be: Started, Success, Failure
def call(String what) {
    call what: what
}
