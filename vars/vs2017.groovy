#!/usr/bin/env groovy

def call(Map m = [:]) {
    def solution = m.get('solution')
    def project = m.get('project')
    def config = m.get('config')
    def command = m.get('command', 'build') // build, clean
    assert solution
    assert project
    assert config

    bat """
        set VSCMD_START_DIR=%CD%
        call \"C:\\Program Files (x86)\\Microsoft Visual Studio\\2017\\Community\\Common7\\Tools\\VsDevCmd.bat\"
        devenv \"${solution}\" /${command} \"${config}\" /project \"${project}\"
    """
}
