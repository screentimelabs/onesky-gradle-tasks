package org.gradle

import org.gradle.api.DefaultTask

import java.security.MessageDigest

/**
 * Created by stevevangasse on 08/07/15.
 */
abstract class OneSkyTask extends DefaultTask {

    def getAuthParams(task) {
        def timestamp = (int)System.currentTimeMillis()/1000

        def devHash = MessageDigest.getInstance("MD5")
                .digest("${timestamp}${project.oneSkyPrivateKey}".bytes).encodeHex().toString()

        task(timestamp, devHash)
    }

    def printResponse = { resp, reader ->
        println "response status: ${resp.statusLine}"
        println 'Headers: -----------'
        resp.headers.each { h ->
            println " ${h.name} : ${h.value}"
        }
        println 'Response data: -----'
        System.out << reader
        println '\n--------------------'
    }
}
