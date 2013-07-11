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

class FtpSpec extends Specification {
    @Shared FtpServer server
    static final File TESTFSWRITEROOT = new File( "${System.getProperty('TESTFSWRITEROOT') ?: 'build/tmp/test-files'}/ftp/dest" )
    
   def vfs = new VFS()
   
   def setupSpec() {
       server = new FtpServer()
       server.start()
   }
   
   def cleanupSpec() {
       server.stop()
   }
   
   def "Can we list files on FTP server"() {
       given:
         def listing = []
         vfs {
             ls (server.READROOT) {
                 listing << it.name.baseName
             }
         }
         println "*** ${listing}"
         
       expect:
         listing.find('file.txt')
         listing.find('file2.txt')
         listing.find('test-subdir')
         
   }
}