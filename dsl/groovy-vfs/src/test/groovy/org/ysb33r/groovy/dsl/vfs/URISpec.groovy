/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2017
 *
 * This software is licensed under the Apache License 2.0
 * See http://www.apache.org/licenses/LICENSE-2.0 for license details
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 * ============================================================================
 */
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

    def "Authority parts should not be stripped"() {
        given:
            URI parent = new URI( 'http://user:pass@some.server/parent/path?vfs.ftp.passiveMode=1&nonVfs=2')

        when:
            URI child = parent / 'child'

        then:
            child.toString() == 'http://user:pass@some.server/parent/path/child?nonVfs=2'
            child.properties.ftp.passiveMode == '1'

    }

    def "Authority parts with encoded passwords should not be stripped"() {
        given:
        URI parent = new URI( 'http://user:{D7B82198B272F5C93790FEB38A73C7B8}@some.server/parent/path?vfs.ftp.passiveMode=1&nonVfs=2')

        when:
        URI child = parent / 'child'

        then:
        child.toString() == 'http://user:{D7B82198B272F5C93790FEB38A73C7B8}@some.server/parent/path/child?nonVfs=2'
        child.properties.ftp.passiveMode == '1'

    }
}