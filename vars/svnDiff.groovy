#!/usr/bin/env groovy

def svnDiffUnix(svnCmd, regex) {
    def shellCmd = "${svnCmd} | awk '{print \$2}'"
    if (regex) {
        // The last part " || :" is to revent grep from exiting 1 in case of nomatch
        // See: https://unix.stackexchange.com/questions/330660/prevent-grep-from-exiting-in-case-of-nomatch
        shellCmd = "${shellCmd} | grep -E '${regex}' || :"
    }

    def stdout = sh(script: shellCmd, returnStdout: true).trim()
    // This if condition is to avoid returning ['']
    // See: https://stackoverflow.com/questions/26716677/groovy-why-does-spliting-an-empty-string-by-space-return-a-list-of-one-empty
    if (stdout) {
        return stdout.split('\n')
    } else {
        return []
    }
}

def svnDiffWindows(svnCmd, regex) {
    def batCmd = """
        @echo off
        ${svnCmd}
    """

    def stdout = bat(script: batCmd, returnStdout: true).trim()
    def lines = stdout.split('\n').collect {it.trim()}
        
    def pattern = null
    if (regex) {
        pattern = ~"${regex}"
    }

    def result = []   
    lines.each { line ->
        if (line) {
            def (mark, file) = line.split('\\s+').collect {it.trim()}
            if (pattern) {
                if (file =~ pattern) {
                    result.add(file)
                }
            } else {
                result.add(file)
            }
        }
    }

    return result
}

def call(Map m = [:]) {
    def from = m.get('from')
    def to = m.get('to', 'HEAD')
    def regex = m.get('regex')

    assert from

    withCredentials([usernamePassword(
        credentialsId: svnCheckout.getCredentialsId(),
        usernameVariable: 'SVN_USERNAME',
        passwordVariable: 'SVN_PASSWORD')]) {

        def svnCmd = "svn diff -r ${from}:${to} --summarize --username ${SVN_USERNAME} --password ${SVN_PASSWORD}"
        if (isUnix()) {
            return svnDiffUnix(svnCmd, regex)
        } else {
            return svnDiffWindows(svnCmd, regex)
        }
    }
}
