#!/usr/bin/env groovy

import java.net.URLEncoder

def call(Map map) {
    assert map
    map.collect { k,v -> URLEncoder.encode(k, 'UTF-8') + '=' + URLEncoder.encode(v, 'UTF-8') }.join('&')
}
