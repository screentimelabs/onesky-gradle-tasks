package org.gradle

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import org.apache.commons.io.IOUtils
import org.gradle.api.tasks.TaskAction
import java.util.concurrent.Executors

import static groovyx.net.http.Method.GET

class OneSkyDownloadTask extends OneSkyTask {

    def projectId
    def files
    def locales
    def destPath
    def threads = 10

    @TaskAction
    def get() {

        def http = new HTTPBuilder("https://platform.api.onesky.io/1/projects/${projectId}/translations")

        getAuthParams { timestamp, devHash ->

            def tasks = []

            locales.each { locale ->
                files.each { translationFile ->
                    tasks.add {
                        def srcFile = new File("${project.rootDir}${translationFile}")
                        println "Downloading translations for ${srcFile}"

                        http.request(GET, ContentType.TEXT) { req ->

                            uri.query = [
                                    api_key         : project.oneSkyPublicKey,
                                    timestamp       : timestamp,
                                    dev_hash        : devHash,
                                    locale          : locale,
                                    source_file_name: srcFile.name
                            ]

                            response.success = { resp, reader ->
                                def languageCode = locale.substring(0, 2) // We're only interested in language at the moment e.g. es-ES becomes es
                                def destFile = new File(project.rootDir, destPath(translationFile, languageCode))
                                println "Saving '${locale}' translation to ${destFile}"
                                def fileWriter = new FileWriter(destFile)
                                if (reader != null) {
                                    IOUtils.copy(reader, fileWriter)
                                    reader.close()
                                }
                                fileWriter.close()
                            }
                            response.failure = printResponse
                        }
                    }
                }
            }

            Executors.newFixedThreadPool(threads).invokeAll(tasks)

        }
    }

    public void destPath(Closure closure) {
        destPath = closure
    }
}
