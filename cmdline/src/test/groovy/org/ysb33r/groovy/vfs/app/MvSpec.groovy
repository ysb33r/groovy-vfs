// ============================================================================
// (C) Copyright Schalk W. Cronje 2014
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================
package org.ysb33r.groovy.vfs.app

import spock.lang.*
import org.ysb33r.groovy.dsl.vfs.URI as vfsURI
import org.ysb33r.groovy.dsl.vfs.VFS
import static org.apache.commons.vfs2.Selectors.EXLUDE_SELF

class MvSpec extends Specification {

    static final File COPYROOT= new File("${System.getProperty('TESTFSREADROOT')}/test-files")
    static final File SRCROOT= new File("${System.getProperty('TESTFSWRITEROOT')}/test-files/mv-source")
    static final File DESTROOT= new File("${System.getProperty('TESTFSWRITEROOT')}/test-files/mv-dest")

    def vfs = new VFS()

    void setup() {
        [ SRCROOT, DESTROOT ].each {
            if(it.exists()) {
                it.deleteDir()
            }
            it.mkdirs()
        }

        vfs {
            cp COPYROOT, SRCROOT, recursive:true, overwrite:false , filter: exclude_self
        }
    }

    void cleanup() {
    }

    def "Plain move (no options) should move the file" () {
        given:
            File from= new File(SRCROOT,'file1.txt')
            File to=   new File(DESTROOT,'file1.txt')
            List<vfsURI> uris = [ new vfsURI( from ),new vfsURI( to.parentFile )  ]
            Mv cmd = new Mv( 'uris' : uris )

        when:
            cmd.run(vfs)

        then:
            !from.exists()
            to.exists()
    }


}