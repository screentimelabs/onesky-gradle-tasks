package org.gradle

import groovyx.net.http.HTTPBuilder
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.gradle.api.tasks.TaskAction

import static groovyx.net.http.Method.POST

class OneSkyUploadTask extends OneSkyTask {

    def projectId
    def format
    def files

    @TaskAction
    def put() {

        def http = new HTTPBuilder("https://platform.api.onesky.io/1/projects/${projectId}/files")

        getAuthParams { timestamp, devHash ->

            files.each { filename ->
                File file = new File("${project.rootDir}${filename}")
                println "Uploading ${file}"

                http.request(POST) { req ->

                    uri.query = [
                            api_key  : project.oneSkyPublicKey,
                            timestamp: timestamp,
                            dev_hash : devHash,
                            file_format: format,
                            is_keeping_all_strings: 'false'
                    ]

                    requestContentType: "multipart/form-data"

                    MultipartEntity multiPartContent = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE)

                    FileBody bin = new FileBody(file);
                    multiPartContent.addPart("file", bin );

                    req.setEntity(multiPartContent)

                    response.success = printResponse
                    response.failure = printResponse
                }
            }
        }
    }
}