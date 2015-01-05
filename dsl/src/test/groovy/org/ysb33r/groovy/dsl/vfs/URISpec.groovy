
package org.ysb33r.groovy.dsl.vfs

import spock.lang.Specification


/**
 * @author Schalk W. Cronjé
 */
class URISpec extends Specification {

    def "URI needs to apply divide operator as relative path separator"() {
        given:
            URI parent = new URI( 'http://some.server/parent/path?vfs.ftp.passiveMode=1&nonVfs=2')

        when:
            URI child = parent / 'child'

        then:
            child.toString() == 'http://some.server/parent/path/child?nonVfs=2'
            child.properties.ftp.passiveMode == '1'
    }

    def "Multiple child paths should append correctly"() {
        given:
            URI parent = new URI( 'http://some.server/parent/path?vfs.ftp.passiveMode=1&nonVfs=2')

        when:
            URI child = parent / 'child1' / "child${2}" / 'child3'

        then:
            child.toString() == 'http://some.server/parent/path/child1/child2/child3?nonVfs=2'
            child.properties.ftp.passiveMode == '1'
    }

    def "Adding fragments via divide operator will cause an exception"() {
        given:
           URI parent = new URI( 'http://some.server/parent/path?vfs.ftp.passiveMode=1&nonVfs=2')

        when:
            URI child = parent / 'child#foo'

        then:
            thrown(URIException)
    }

    def "Adding query parameters via divide operator will cause an exception"() {
        given:
            URI parent = new URI( 'http://some.server/parent/path?vfs.ftp.passiveMode=1&nonVfs=2')

        when:
            URI child = parent / 'child?foo'

        then:
            thrown(URIException)
    }

}