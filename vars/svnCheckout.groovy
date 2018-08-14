#!/usr/bin/env groovy

def getRealm() {
    return '<svn://svn.xsjme.com:3690> svnrepo'
}

def getCredentialsId() {
    return 'subversion'
}

def getRemote(root, folder) {
    def gitBranchName = env.BRANCH_NAME ?: 'master'
    def svnBranchName = gitBranchName == 'master' ? 'trunk' : "branches/${gitBranchName}"
    def remote = "${root}/${svnBranchName}"
    return folder ? "${remote}/${folder}" : remote
}

def getUpdater(revertBeforeUpdate) {
    return revertBeforeUpdate ? 'UpdateWithRevertUpdater' : 'UpdateUpdater'
}

// Returns: the return value of the checkout step, which is a map.
// Example of available values:
//  SVN_REVISION=42998
//  SVN_URL=svn://svn.xsjme.com/rog2/pirates/trunk/server
def call(Map params = [:]) {
    def root = params.get('root', 'svn://svn.xsjme.com/rog2/pirates')
    def folder = params.get('folder', '')
    assert folder
    def local = params.get('local', folder)
    def revertBeforeUpdate = params.get('revertBeforeUpdate', false)
    def ignoreExternals = params.get('ignoreExternals', false)
    def poll = params.get('poll', true)
    def changelog = params.get('changelog', true)
    def depth = params.get('depth', 'infinity')

    def svnSCM = [$class: 'SubversionSCM',
        locations: [[
            credentialsId: getCredentialsId(),
            remote: getRemote(root, folder),
            local: local,
            depthOption: depth,
            ignoreExternalsOption: ignoreExternals,
        ]],
        additionalCredentials: [[
            realm: getRealm(),
            credentialsId: getCredentialsId()
        ]],
        quietOperation: true,
        workspaceUpdater: [$class: getUpdater(revertBeforeUpdate)]
    ]

    return checkout(scm: svnSCM, poll: poll, changelog: changelog)
}
