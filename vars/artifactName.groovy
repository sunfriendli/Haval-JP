#!/usr/bin/env groovy

// 
// Centralize artifact naming pattern.
// 

def call(Map params = [:]) {
    def name = params.get('name', '')
    def suffix = params.get('suffix', '')
    def extension = params.get('extension', '')

    assert name
    assert extension

    assert env.BUILD_NUMBER
    if (!env.BUILD_DATE) {
        error '''
        [artifactName] BUILD_DATE is missing.
        This library depends on "Build Timestamp" (https://plugins.jenkins.io/build-timestamp) plugin to work.
        Please export env variable BUILD_DATE as yyyyMMdd in Global Configure page.
        '''
    }
    def gitBranchName = env.BRANCH_NAME ?: 'master'

    return "${name}-${gitBranchName}-b${env.BUILD_NUMBER}-${env.BUILD_DATE}${suffix}.${extension}"
}
