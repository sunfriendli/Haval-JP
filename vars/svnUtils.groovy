#!/usr/bin/env groovy

def rawCmd(Map params = [:]) {
    def inCmd = params.get('inCmd', '')
    def outCmd = params.get('outCmd', '')
    def svnCmd = params.get('svnCmd')
    assert svnCmd

    def stdout = null
    withCredentials([usernamePassword(
        credentialsId: svnCheckout.getCredentialsId(),
        usernameVariable: 'SVN_USERNAME',
        passwordVariable: 'SVN_PASSWORD')]) {

        def cmd = "${svnCmd} --username ${SVN_USERNAME} --password ${SVN_PASSWORD}"
        if (inCmd) {
            cmd = "${inCmd} | " + cmd
        }
        if (outCmd) {
            cmd = cmd + " | ${outCmd}"
        }

        if (isUnix()) {
            stdout = sh(script: cmd, returnStdout: true).trim()
        } else {
            def batCmd = """
                @echo off
                ${cmd}
            """
            stdout = bat(script: batCmd, returnStdout: true).trim()
        }
    }

    return stdout
}

def rawCmd(String cmd) {
    return rawCmd(svnCmd: cmd)
}

// ============== for example: svn info src ==============
// Path: src
// Working Copy Root Path: C:\xxx\pirates\trunk
// URL: svn://svn.xsjme.com/rog2/pirates/trunk/src
// Relative URL: ^/pirates/trunk/src
// Repository Root: svn://svn.xsjme.com/rog2
// Repository UUID: 2834c61a-5f6e-49b0-bc65-1ef817db6f98
// Revision: 45871
// Node Kind: directory
// Schedule: normal
// Last Changed Author: xxx
// Last Changed Rev: 45871
// Last Changed Date: 2018-06-06 10:20:58 +0800 (Wed, 06 Jun 2018)
// ========================================================
def svnInfo(folder) {
    def stdout = rawCmd("svn info ${folder}")
    def lines = stdout.split('\n').collect {it.trim()}

    def result = [:]
    lines.each { line ->
        def i = line.indexOf(':')
        if (i > 0) {
            result[line.substring(0, i).trim()] = line.substring(i + 1).trim()
        }
    }

    return result 
}

// http://svnbook.red-bean.com/en/1.8/svn.ref.svn.c.status.html, for example:
// ?       1.txt
// M       2.txt
// !       3.txt
def svnStatus(folder) {
    def stdout = rawCmd("svn st ${folder}")
    def lines = stdout.split('\n').collect {it.trim()}

    def result = [:]
    lines.each { line ->
        if (line) {
            def mark = line.substring(0, 1).trim()
            def file = line.substring(1).trim()

            def list = result.find { it.key == mark }?.value
            if (!list) {
                list = []
                result[mark] = list
            }
            list.add(file)
        }
    }

    return result
}

def svnUpdateWithExcludeFolder(Map params = [:]) {
    def root = params.get('root', 'svn://svn.xsjme.com/rog2/pirates')
    def folder = params.get('folder', '')
    assert folder
    def local = params.get('local', folder)
    def relativeExcludeFolder = params.get('relativeExcludeFolder')
    assert relativeExcludeFolder

    def needCheckout = false
    try {
        def folderRemoteUrl = svnCheckout.getRemote(root, folder)
        def localInfo = svnInfo(local)
        if (localInfo['URL'] != folderRemoteUrl) {
            needCheckout = true
        }
    } 
    catch(ex) {
        echo ex.message
        needCheckout = true
    }

    if (needCheckout) {
        svnCheckout(
            root: root, folder: folder, local: local, revertBeforeUpdate: false, changelog: false, poll: false
        )
    }

    def excludeFolderLocalPath = "${local}/${relativeExcludeFolder}"
    try {
        def excludeFolderRemoteUrl = svnCheckout.getRemote(root, "${folder}/${relativeExcludeFolder}")       
        // if excludeFolderLocalPath has been excluded, svn info excludeFolderLocalPath will show a additional line:
        // Depth: exclude
        def info = svnInfo(excludeFolderLocalPath)
        if (info['Depth'] != 'exclude' && info['URL'] == excludeFolderRemoteUrl) {
            echo rawCmd("svn up --set-depth exclude ${excludeFolderLocalPath}")
        }

        echo rawCmd("svn revert -R ${local}")
        echo rawCmd("svn up ${local}")
    } 
    catch(ex) {
        echo ex.message
        echo "Fail to update ${folder}, try to re-checkout"

        svnCheckout(
            root: root, folder: folder, local: local, revertBeforeUpdate: true, changelog: false, poll: false
        )

        echo rawCmd("svn up --set-depth exclude ${excludeFolderLocalPath}")
    }
}
