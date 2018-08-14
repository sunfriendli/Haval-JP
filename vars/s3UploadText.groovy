#!/usr/bin/env groovy

def call(Map params = [:]) {
    def credential = params.get('credential')
    def region = params.get('region')
    def bucket = params.get('bucket')
    def text = params.get('text', '')
    def file = params.get('file', '')
    def prefix = params.get('prefix', '')
    def contentType = params.get('contentType', 'text/plain;charset=utf-8')
    def acl = params.get('acl', 'Private')
    assert credential
    assert region
    assert bucket
    assert text
    assert file

    def s3Path = prefix ? "${prefix}/${file}" : file
    writeFile encoding: 'UTF-8', file: file, text: text
    withAWS(credentials: credential, region: region) {
        s3Upload(file: file,
            bucket: bucket,
            path: s3Path,
            contentType: contentType,
            acl: acl,
        )
    }
}
