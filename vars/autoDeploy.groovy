#!/usr/bin/env groovy

// 
// Auto deploys service in dev environment.
// 

def getTargetEnv() {
    def gitBranchName = env.BRANCH_NAME ?: 'master'
    return gitBranchName == 'master' ? 'dev' : 'test'
}

def call(String service) {
    assert service
    assert env.JOB_NAME
    assert env.BUILD_NUMBER

    build(job: "Pirates/Ops/Deploy/${service}",
        parameters: [
            string(name: 'PROJECT_NAME', value: env.JOB_NAME),
            string(name: 'PROJECT_BUILD_NUMBER', value: env.BUILD_NUMBER),
            string(name: 'TARGET_ENV', value: getTargetEnv())
        ]
    )
}
