#!/usr/bin/env groovy

// 
// Upload single file in workspace to AWS S3, with enhanced behavior:
//  - Supports wildcard with local file name (will only deal with first file found)
//  - Will skip uploading if file already exists on S3
//  - Returns the S3Uri of the file, example: s3://pirates/prod/hubserver/135/hubserver-trunk-build-135.tar.gz 
//

// Converts s3:// to https://
def s3UriToDownloadUrl(String s3Uri, String region) {
    assert s3Uri
    assert region
    assert s3Uri.startsWith('s3://')

    if (region.startsWith('cn-')) {
        return s3Uri.replaceFirst('s3://', "https://s3.${region}.amazonaws.com.cn/")
    } else if (region == 'us-east-1') {
        return s3Uri.replaceFirst('s3://', 'https://s3.amazonaws.com/')
    } else {
        return s3Uri.replaceFirst('s3://', "https://s3-${region}.amazonaws.com/")
    }
}

def call(Map params = [:]) {
    def glob = params.get('glob', '')
    assert glob
    def credential = params.get('credential', 'aws-cn')
    def region = params.get('region', 'cn-north-1')
    def bucket = params.get('bucket', '')
    assert bucket
    def prefix = params.get('prefix', '')
    assert prefix
    def acl = params.get('acl', 'Private')
    def returnDownloadUrl = params.get('returnDownloadUrl', false)

    def files = findFiles(glob: glob)
    assert files.size() > 0
    echo "[s3UploadEx] ${files[0].path} (${files[0].length} bytes)"
    def fileName = files[0].name
    def filePath = files[0].path

    withAWS(credentials: credential, region: region) {
        def s3Bucket = bucket
        def s3Path = "${prefix}/${fileName}"
        def s3Uri = "s3://${s3Bucket}/${s3Path}"
        def s3Files = s3FindFiles bucket: s3Bucket, glob: s3Path, onlyFiles: true
        if (s3Files.size() > 0) {
            echo "[s3UploadEx] ${s3Uri} already exist, skip upload."
        } else {
            s3Upload file: filePath, bucket: s3Bucket, path: s3Path, acl: acl
        }
        return returnDownloadUrl ? s3UriToDownloadUrl(s3Uri, region) : s3Uri
    }
}
