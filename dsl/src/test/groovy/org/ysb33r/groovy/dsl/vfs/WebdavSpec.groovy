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

import org.ysb33r.groovy.dsl.vfs.services.FtpServer
import org.ysb33r.groovy.dsl.vfs.services.WebdavServer
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification

import static org.ysb33r.groovy.dsl.vfs.helpers.ListFolderTestHelper.getAssertListable

class WebdavSpec extends SchemaSpec  {

    def setupSpec() {
        server = new WebdavServer()
        server.start()
    }
    
    def cleanupSpec() {
        server?.stop()
    }
 

   def "Can we list files on Webdav server "() {
       expect:
           assertListable vfs, "${server.READROOT}"
           assertListable vfs, "${server.READROOT}"
           assertListable vfs, "${server.READROOT}"
           assertListable vfs, "${server.READROOT}"
   }

}
