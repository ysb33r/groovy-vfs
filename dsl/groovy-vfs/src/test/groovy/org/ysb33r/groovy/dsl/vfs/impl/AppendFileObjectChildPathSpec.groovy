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
package org.ysb33r.groovy.dsl.vfs.impl

import org.ysb33r.vfs.dsl.groovy.Vfs
import org.ysb33r.vfs.test.services.FtpServer
import spock.lang.Shared
import spock.lang.Specification


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
        vfs= new Vfs()
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