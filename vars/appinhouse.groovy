#!/usr/bin/env groovy

// 
// Publish application metadata and download URL to appinhouse service.
// https://apps.rog2.org/
// 

def call(Map params = [:]) {
    def app = params.get('app', '')
    def version = params.get('version', '')
    def platform = params.get('platform', '')
    def description = params.get('description', '')
    def buildUrl = params.get('buildUrl', '')
    def downloadUrl = params.get('downloadUrl', '')

    // May be deprecated later
    def channel = params.get('channel', '')
    def environment = params.get('environment', 'dev')

    // Extra download url
    def downloadUrlId = params.get('downloadUrlId', '')
    def downloadUrlDisplayName = params.get('downloadUrlDisplayName', '')

    // iOS specific params
    def iosBundleId = params.get('iosBundleId', '')
    def iosTitle = params.get('iosTitle', '')

    assert app
    assert version
    assert platform
    assert description
    assert buildUrl
    assert downloadUrl
    assert env.BUILD_TIMESTAMP  // For 'time'
    assert channel

    // See: https://github.com/rog2/appinhouse/blob/master/doc/api.md
    def form = [
        'version': version,
        'description': description,
        'time': env.BUILD_TIMESTAMP,
        'channel': channel,
        'url': buildUrl,
        'software_url': downloadUrl
    ]
    if (downloadUrlId && downloadUrlDisplayName) {
        form['software_url_extend_key'] = downloadUrlId
        form['software_url_extend_name'] = downloadUrlDisplayName
    }
    if (platform == 'ios') {
        assert iosBundleId
        assert iosTitle
        form['id'] = iosBundleId
        form['title'] = iosTitle
    }
    def body = urlEncode(form)
    echo "[appinhouse] HTTP post body:\n${body}"

    def defaults = loadEnv.getDefaults()
    withCredentials([string(credentialsId: defaults['appinhouse.credential'], variable: 'APPINHOUSE_SECRET_KEY')]) {
        def endpoint = defaults['appinhouse.endpoint']
        def url = "${endpoint}/api/${app}/desc/${platform}/${environment}"

        def response = httpRequest(url: url,
            httpMode: 'POST',
            contentType: 'APPLICATION_FORM',
            customHeaders: [[name: 'X-SecretKey', value: env.APPINHOUSE_SECRET_KEY, maskValue: true]],
            requestBody: body
        )

        echo "[appinhouse] HTTP status ${response.status}"
        echo "[appinhouse] HTTP content:\n${response.content}"

        def appUrl = "${endpoint}/info.html?app=${app}&platform=${platform}&environment=${environment}&version=${version}"
        return appUrl
    }
}
