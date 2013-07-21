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

package org.ysb33r.groovy.dsl.vfs


import spock.lang.*
import org.ysb33r.groovy.dsl.vfs.services.*
import org.ysb33r.groovy.dsl.vfs.helpers.*
import static org.ysb33r.groovy.dsl.vfs.helpers.ListFolderTestHelper.*
import org.ysb33r.groovy.dsl.vfs.FileActionException

class FtpSpec extends SchemaSpec /*Specification*/ {
    
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
 
}
