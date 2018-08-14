#!/usr/bin/env groovy

//
// Build Unreal Engine 4 (UE4Editor) on Linux/Mac/Windows.
//

def getUrl() {
    return 'git://github-mirror.intranet.rog2.org/rog2/UnrealEngine.git'
}

def getGitHubUrl() {
    return 'https://github.com/rog2/UnrealEngine'
}

def getGitDepsUrl(String engineVersion) {
    return "https://s3.intranet.rog2.org/software/ue4/gitdeps/${engineVersion}/ue4-gitdeps.zip"
}

def getBranch() {
    def gitBranchName = env.BRANCH_NAME ?: 'master'
    return gitBranchName == 'master' ? 'release' : gitBranchName
}

def ue4Checkout(String localDir, String sys) {
    // Workaround for windows file path length limitation,
    // Otherwise you may see: Filename too long error: unable to create file
    // This assumes "Git for Windows" has already been installed on windows slave.
    if (sys == 'Windows') {
        bat 'git config --global core.longpaths true'
    }
    def ue4Revision = checkout([$class: 'GitSCM',
        userRemoteConfigs: [[url: getUrl()]],
        branches: [[name: getBranch()]],
        browser: [$class: 'GithubWeb', repoUrl: getGitHubUrl()],
        extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: localDir]]
    ])
    return ue4Revision
}

def getEngineVersion() {
    def json = readJSON file: 'Engine/Build/Build.version'
    def major = json['MajorVersion']
    def minor = json['MinorVersion']
    def patch = json['PatchVersion']
    return "${major}.${minor}.${patch}"
}

def downloadEngineGitDeps() {
    def engineVersion = getEngineVersion()
    def gitDepsUrl = getGitDepsUrl(engineVersion)
    def zipFileName = httpDownload gitDepsUrl
    unzip zipFile: zipFileName, quiet: true
}

// This is to save disk space, because gitdeps file and folder is huge :)
def removeEngineGitDeps(String sys) {
    if (sys == 'Windows') {
        bat '''
            DEL /Q ue4-gitdeps.zip
            RMDIR /S /Q ue4-gitdeps
        '''
    } else {
        sh 'rm -rf ue4-gitdeps*'
    }
}

def ue4Setup(String sys, String setupArgs) {
    downloadEngineGitDeps()
    setupArgs = "--cache=ue4-gitdeps ${setupArgs}"
    if (sys == 'Windows') {
        // --force is to always overwrite modified files, because windows lacks 'yes' utility
        // SetupUnattended.bat is modified from Setup.bat to skip steps that require human input
        bat "SetupUnattended.bat ${setupArgs} --force"
    } else {
        sh "yes | ./Setup.sh ${setupArgs}"
    }
    removeEngineGitDeps(sys)
}

def buildEngineMac() {
    // TODO: Add implementation for Mac
    throw new UnsupportedOperationException()
}

def buildEngineLinux() {
    sh '''
        ./GenerateProjectFiles.sh
        make UE4Editor UnrealPak ShaderCompileWorker
    '''
}

def buildEngineWindows() {
    bat 'GenerateProjectFiles.bat -2017'
    // https://intranet.rog2.org/docs/test-guides/problem-collect-index#%E5%BC%95%E6%93%8E%E9%97%AE%E9%A2%98
    // UE4 bug: When engine is updated, the scene can't be baked on windows platform, the solution is rebuild UnrealLightmass before build engine
    vs2017 command: 'rebuild', solution: 'UE4.sln', project: 'UnrealLightmass', config: 'Development Editor'
    vs2017 command: 'build', solution: 'UE4.sln', project: 'UE4', config: 'Development Editor'
    // When editor's uproject file is updated, UnrealVersionSelector will be used to prompt user
    // to select engine path and make association with editor, then re-generate sln file, which is noisy to non-dev user. 
    // I will modify the implementation of UnrealVersionSelector to automate this procedure without user's input and avoid generate sln. 
    // The specific code will be committed into rog2/UnrealEngine, here just for build.
    vs2017 command: 'build', solution: 'UE4.sln', project: 'UnrealVersionSelector', config: 'Development Editor'
}

def ue4Build(String sys) {
    if (sys == 'Linux') {
        buildEngineLinux()
    } else if (sys == 'Windows') {
        buildEngineWindows()
    } else if (sys == 'Mac') {
        buildEngineMac()
    } else {
        error "Unknown system: ${sys}"
    }
}

// Returns: the return value of the checkout step, which is a map.
// See: https://plugins.jenkins.io/git
// Example of available values:
//  GIT_BRANCH=origin/master
//  GIT_COMMIT=d8ce983b39947fb9cb600c1f6e0ee2a64d172b0c
//  GIT_LOCAL_BRANCH=master
//  GIT_URL=https://github.com/rog2/jenkins-slave-linux
def call(String localDir = 'UnrealEngine', String setupArgs = '', boolean forceBuild = false) {
    def sys = uname()
    def ue4CleanBuild = !fileExists(localDir)
    def ue4Revision = ue4Checkout(localDir, sys)
    dir(localDir) {
        if (ue4CleanBuild) {
            ue4Setup(sys, setupArgs)
        } else {
            echo 'Not doing a UnrealEngine clean build, skip setup process.'
        }
        if (forceBuild || ue4CleanBuild || ue4Revision.GIT_COMMIT != ue4Revision.GIT_PREVIOUS_SUCCESSFUL_COMMIT) {
            ue4Build(sys)
        } else {
            echo 'Skip build UnrealEngine.'
        }
    }
    return ue4Revision
}
