/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2015
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

import org.apache.commons.vfs2.FileObject
import org.ysb33r.groovy.dsl.vfs.impl.Util
import spock.lang.*
import org.ysb33r.groovy.dsl.vfs.services.*
import org.ysb33r.groovy.dsl.vfs.helpers.*
import static org.ysb33r.groovy.dsl.vfs.helpers.ListFolderTestHelper.*
import org.ysb33r.groovy.dsl.vfs.FileActionException

class FtpSpec extends SchemaSpec  {
    
    def setupSpec() {
        server = new FtpServer()
        server.start()
    }
    
    def cleanupSpec() {
        server.stop()
    }
 

        
   def "Can we list files on FTP server "() {
       expect:
           assertListable vfs, "${server.READROOT}?vfs.ftp.passiveMode=1"
           assertListable vfs, "${server.READROOT}?vfs.ftp.passiveMode=0"
           assertListable vfs, "${server.READROOT}?vfs.ftp.passiveMode=false"
           assertListable vfs, "${server.READROOT}?vfs.ftp.passiveMode=true"
   }

   def "Can we apply the '/' operator"() {
       given:
        def vfs = super.vfs
        def child

       when:
           vfs {
               def u = stageURI ("${server.READROOT}?vfs.ftp.passiveMode=1")
               child = u / 'file1.txt'
           }

       then:
            child.toString() == "${server.READROOT}/file1.txt"
            child instanceof URI

   }

    def "Resolving a resolved URI with encoded password must resolve correctly"() {
        given: "A previously resolved URI"
        FileObject firstResolve = vfs.resolveURI("ftp://guest:{6D9D9D4A32C1C3F9B0FCDC0162476BAA}@localhost:${server.PORT}/test-subdir?vfs.ftp.passiveMode=1")

        when:
        def secondResolve = vfs.resolveURI(firstResolve)

        then:
        secondResolve != null

        when:
        def thirdResolve = Util.resolveURI(
            [ filter : ~/.+/ ],
            firstResolve.fileSystem.fileSystemManager,
            firstResolve.fileSystem.fileSystemOptions,
            firstResolve.name.getURI()
        )

        then:
        thirdResolve != null
    }

    def "Listing a directory with a resolved URI and additional non-VFS options"() {
        given: "A previously resolved URI"
        def resolvedUri = vfs.resolveURI("ftp://guest:{6D9D9D4A32C1C3F9B0FCDC0162476BAA}@127.0.0.1:${server.PORT}/test-subdir?vfs.ftp.passiveMode=1&vfs.ftp.fooParam=1")
        List<String> names = []

        when:
        vfs.ls resolvedUri, filter : ~/.+/, { FileObject fo ->
            names+= fo.name.baseName
        }

        then:
        names.size()
    }

}
