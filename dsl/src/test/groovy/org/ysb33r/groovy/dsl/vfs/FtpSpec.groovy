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


import java.io.File;

import spock.lang.*
import org.ysb33r.groovy.dsl.vfs.services.*
import org.ysb33r.groovy.dsl.vfs.helpers.*
import static org.ysb33r.groovy.dsl.vfs.helpers.ListFolderTestHelper.*
import org.apache.commons.logging.impl.SimpleLog

class FtpSpec extends Specification {
    @Shared FtpServer server
    
   def vfs = new VFS( logger: new SimpleLog('FtpSpec'))
   
   def setupSpec() {
       server = new FtpServer()
       server.start()
   }
   
   def cleanupSpec() {
       server.stop()
   }
   
   def setup() {
       def simpleLog = new SimpleLog('FtpSpec')
       simpleLog.setLevel( SimpleLog.LOG_LEVEL_ALL )
       def vfs = new VFS( logger: simpleLog )
       vfs.getLogger().debug "FtpSpec logger up and running"
   }
   def "Can we list files on FTP server - old way"() {
       given:
         def listing = [:]
         vfs {
             ls (server.READROOT) {
                 listing."${it.name.baseName}"= 1
             }
         }
         
       expect:
         listing.'file1.txt' == 1
         listing.'file2.txt' == 1
         listing.'test-subdir' == 1
         
   }
   
   def "Can we list files on FTP server - new way"() {
       expect:
           assertListable vfs, server.READROOT
           assertListable vfs, "${server.READROOT}?vfs.ftp.passiveMode=1"
           assertListable vfs, "${server.READROOT}?vfs.ftp.passiveMode=0"
   }
}