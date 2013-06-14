// ============================================================================
// (C) Copyright Schalk W. Cronje 2013
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================

import spock.lang.*
import org.ysb33r.gradle.vfs.Vfs
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class VfsSpec extends spock.lang.Specification {
    def Project project = ProjectBuilder.builder().build()
    def testVFS = project.task('vfs', type: Vfs )
    
    def "Can add Vfs task to project"() {
        given:
            testVFS.getVFS().metaClass.getMgr = { fsMgr }
            def fsMgr = testVFS.getVFS().getMgr()
            
        expect:
            testVFS instanceof Vfs  
            testVFS.getVFS() instanceof org.ysb33r.groovy.dsl.vfs.VFS
            fsMgr instanceof org.apache.commons.vfs2.impl.StandardFileSystemManager
    }
    
    def "Must be able to set vfs style properties via the task's configure block"() {
        given:
            testVFS {
                ftp {
                    passiveMode true
                }
            }
            
        expect:
            testVFS.getVFS() == null 
    }
}

