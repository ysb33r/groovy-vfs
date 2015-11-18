package org.ysb33r.groovy.vfs.test.services

import spock.lang.Shared
import spock.lang.Specification


/**
 * @author Schalk W. Cronjé
 */
class WebdavServerSpec extends Specification {

    static final File SERVERROOT = new File('./build/tmp/webdav').absoluteFile

    @Shared WebdavServer server

    void setupSpec() {
        if(SERVERROOT.exists()) {
            SERVERROOT.deleteDir()
        }
        SERVERROOT.mkdirs()
        server = new WebdavServer(
            homeFolder : SERVERROOT
        )
    }

    void cleanup() {
        server?.stop()
    }

    def "Start up the Webdav server and ensure it accepts requests"() {

        when:
            server.start()

        then:
            true
    }

}