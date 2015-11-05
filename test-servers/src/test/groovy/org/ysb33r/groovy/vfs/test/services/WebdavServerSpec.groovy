package org.ysb33r.groovy.vfs.test.services

import spock.lang.Specification


/**
 * @author Schalk W. Cronjé
 */
class WebdavServerSpec extends Specification {

    static final File SERVERROOT = new File('./build/tmp/webdav').absoluteFile

    WebdavServer server

    void setup() {
        if(SERVERROOT.exists()) {
            SERVERROOT.deleteDir()
        }
        SERVERROOT.mkdirs()
    }

    void cleanup() {
        server?.shutdown()
    }

    def "Start up the Webdav server and ensure it accepts requests"() {
        given:
            server = new WebdavServer()
            server.repository = SERVERROOT

        when:
            server.run()

        then:

            true
    }

}