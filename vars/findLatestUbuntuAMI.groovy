#!/usr/bin/env groovy

def call(Map m = [:]) {

    // =======================================================================

    // See: https://cloud-images.ubuntu.com/locator/ec2/
    def AMI_LOCATOR_URL = 'https://cloud-images.ubuntu.com/locator/ec2/releasesTable'

    // Extract ami-05596fb52c3802012 from <a href="https://xxx">ami-05596fb52c3802012</a>
    def AMI_ID_REGEX = '>([^<]+)<'

    def OPT_ZONE          = 0
    def OPT_NAME          = 1
    def OPT_VERSION       = 2
    def OPT_ARCH          = 3
    def OPT_INSTANCE_TYPE = 4
    def OPT_RELEASE       = 5
    def OPT_ANCHOR        = 6

    // =======================================================================

    def optZone = m.get('zone', 'cn-north-1')
    def optName = m.get('name', 'bionic')
    def optVersion = m.get('version', '18.04 LTS')
    def optArch = m.get('arch', 'amd64')
    def optInstanceType = m.get('instanceType', 'hvm:ebs-ssd')

    // =======================================================================

    def response = httpRequest AMI_LOCATOR_URL
    assert response.status == 200

    def content = readJSON text: response.content

    def ami = content.aaData.find {
        it[OPT_ZONE] == optZone &&
        it[OPT_NAME] == optName &&
        it[OPT_VERSION] == optVersion &&
        it[OPT_ARCH] == optArch &&
        it[OPT_INSTANCE_TYPE] == optInstanceType
    }

    // =======================================================================

    if (ami == null) {
        return null
    }

    def anchor = ami[OPT_ANCHOR]
    def match = (anchor =~ AMI_ID_REGEX)
    def id = match[0][1]
    def release = ami[OPT_RELEASE]

    return [
        'id': id,
        'release': release,
        'zone': optZone,
        'name': optName,
        'version': optVersion,
        'arch': optArch,
        'instanceType': optInstanceType
    ]
}
