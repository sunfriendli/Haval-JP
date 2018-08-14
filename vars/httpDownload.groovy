#!/usr/bin/env groovy

def call(String url) {
    assert url
    echo "[httpDownload] ${url}"
    def fileName = url[url.lastIndexOf('/')+1..-1]
    if (isUnix()) {
        sh "wget -q ${url}"
    } else {
        powershell """
            \$ProgressPreference='SilentlyContinue'
            Invoke-WebRequest -Uri ${url} -OutFile ${fileName}
        """
    }
    return fileName
}
