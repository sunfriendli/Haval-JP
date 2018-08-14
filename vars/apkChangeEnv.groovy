#!/usr/bin/env groovy

def findInputApk() {
    def files = findFiles(glob: '*.apk')
    assert files.size() > 0
    def apk = files[0]
    echo "[apkChangeEnv] input apk: ${apk.name}, (${apk.length} bytes)"
    return apk.name
}

def getNameWithoutExt(String name) {
    return name.take(name.lastIndexOf('.'))
}

def call(Map params = [:]) {
    def envName = params.get('env')
    def appmodder = params.get('appmodder')
    def gameName = params.get('gameName')
    def unrealPakVersion = params.get('unrealPakVersion')

    def keystore = params.get('keystore')
    def keystorePassword = params.get('keystorePassword')
    def keyPassword = params.get('keyPassword')
    def keyAlias = params.get('keyAlias')

    assert envName
    assert appmodder
    assert gameName
    assert unrealPakVersion

    assert keystore
    assert keystorePassword
    assert keyPassword
    assert keyAlias

    def targetEnv = loadEnv(envName)
    assert targetEnv

    def skipPatch = 'True'
    def patchUrl = targetEnv['client.url.patch.android']
    def helpUrl = targetEnv['client.url.help']
    def announcementUrl = targetEnv['client.url.announcement']
    def serverlistUrl = targetEnv['client.url.serverlist']

    def inputApkName = findInputApk()
    def outputApkName = "${getNameWithoutExt(inputApkName)}.${envName}.apk"
    assert inputApkName != outputApkName

    withUnrealPak(unrealPakVersion) {
        dir(appmodder) {
            sh """
                python3 ReplaceFilesInApp.py \
                    --platform Android_ETC2 \
                    --game ${gameName} \
                    --input ${env.WORKSPACE}/${inputApkName} \
                    --output ${env.WORKSPACE}/${outputApkName} \
                    --android-keystore ${keystore} \
                    --android-keystore-password ${keystorePassword} \
                    --android-key-password ${keyPassword} \
                    --android-key-alias ${keyAlias} \
                    --task update-ini-value:ini=Pirates/Config/DefaultGame.ini,section=/Script/EngineExt.EngineExtSetting,key=UrlMode,value=${envName} \
                    --task update-ini-value:ini=Pirates/Config/DefaultPatch.ini,section=/Script/IncrementalUpdate.PatchSettings,key=SkipPatch,value=${skipPatch} \
                    --task update-ini-value:ini=Pirates/Config/DefaultUrl.ini,section=/Script/IncrementalUpdate.UrlSettings,quotes=1,key=PatchUrl,value=${patchUrl} \
                    --task update-ini-value:ini=Pirates/Config/DefaultUrl.ini,section=/Script/IncrementalUpdate.UrlSettings,quotes=1,key=HelpUrl,value=${helpUrl} \
                    --task update-ini-value:ini=Pirates/Config/DefaultUrl.ini,section=/Script/IncrementalUpdate.UrlSettings,quotes=1,key=AnnouncementUrl,value=${announcementUrl} \
                    --task update-ini-value:ini=Pirates/Config/DefaultUrl.ini,section=/Script/IncrementalUpdate.UrlSettings,quotes=1,key=ServerListUrl,value=${serverlistUrl} \
            """
        }
    }

    return outputApkName
}
