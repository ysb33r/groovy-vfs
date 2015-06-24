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

class MkdirSpec extends Specification {

    static final File WRITEROOT= new File("${System.getProperty('TESTFSWRITEROOT')}/Mkdir")
    def vfs = new VFS()

    void setup() {
        if(WRITEROOT.exists()) {
            WRITEROOT.deleteDir()
        }
        WRITEROOT.mkdirs()
        assert WRITEROOT.exists()
    }

    def "Create a local directory" () {
        given:
            List<vfsURI> uris = [ new vfsURI( new File(WRITEROOT,'test1') ), new vfsURI( new File(WRITEROOT,'test2') ) ]
            Cmd cmd = new Mkdir( intermediates : false, 'uris' : uris )

        when:
            cmd.run(vfs)

        then:
            new File(WRITEROOT,'test1').exists()
            new File(WRITEROOT,'test2').exists()
    }
}