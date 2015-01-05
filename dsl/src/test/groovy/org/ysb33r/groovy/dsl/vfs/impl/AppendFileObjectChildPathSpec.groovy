
package org.ysb33r.groovy.dsl.vfs.impl

import org.apache.commons.vfs2.VFS
import org.ysb33r.groovy.dsl.vfs.URI
import org.ysb33r.groovy.dsl.vfs.URIException
import org.ysb33r.groovy.dsl.vfs.services.FtpServer
import spock.lang.Shared
import spock.lang.Specification


/**
 * @author Schalk W. Cronjé
 */
class AppendFileObjectChildPathSpec extends Specification {

    @Shared def server
    @Shared def vfs
    @Shared def parent

    def child

    def setup() {
    }

    void setupSpec() {
        server = new FtpServer()
        server.start()
        vfs= VFS.manager
        parent = vfs.resolveFile("${server.READROOT}")
    }

    void cleanupSpec() {
        server.stop()
    }


    def "Child path should append correctly"() {

        when:
            child = Util.addRelativePath(parent,'test-subdir')

        then:
            child.toString() == "${server.READROOT}/test-subdir"
    }

    def "Multiple child paths should append correctly"() {

        when:
            child = Util.addRelativePath(parent, 'test-subdir')
            child = Util.addRelativePath(child, "file${3}.txt")

        then:
            child.toString() == "${server.READROOT}/test-subdir/file3.txt"
    }

    def "Adding fragments will cause an exception"() {

        when:
            Util.addRelativePath(parent,'file1.txt#foo')

        then:
            thrown(URIException)
    }

    def "Adding query parameters will cause an exception"() {

        when:
            Util.addRelativePath(parent,'file1.txt?foo')

        then:
            thrown(URIException)
    }

}