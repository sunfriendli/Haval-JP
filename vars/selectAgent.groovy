#!/usr/bin/env groovy

//
// Return agent label based on env.BRANCH_NAME set by multibranch pipeline.
//

def getLabelPirates() {
    def gitBranchName = env.BRANCH_NAME ?: 'master'
    def label = gitBranchName == 'master' ? 'trunk' : 'branch'
    return "pirates:${label}"
}

def call(String labelExpression) {
    def labelPirates = getLabelPirates()
    labelExpression = "${labelExpression} && ${labelPirates}"
    echo "Selected Agent Label: ${labelExpression}"
    return labelExpression
}
