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

// After an idea from http://snipplr.com/view/64953/

package org.ysb33r.groovy.dsl.vfs.services

import java.io.File;

import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.ftplet.UserManager
import org.apache.ftpserver.ftplet.Authority
import org.apache.ftpserver.listener.ListenerFactory
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor
import org.apache.ftpserver.usermanager.impl.BaseUser
import org.apache.ftpserver.usermanager.impl.WritePermission

class FtpServer {

    static final def PORT  = System.getProperty('FTPPORT') ?: 50021   
    static final def READROOT    = "ftp://guest:guest@localhost:${PORT}"
    static final def ARCHIVEROOT = "ftp://archive:archive@localhost:${PORT}"
    static final def WRITEROOT   = "ftp://root:root@localhost:${PORT}"
    static final File TESTFSREADONLYROOT  = new File("${System.getProperty('TESTFSREADROOT')}/src/test/resources/test-files")
    static final File TESTFSWRITEROOT     = new File("${System.getProperty('TESTFSWRITEROOT') ?: 'build/tmp/test-files'}/ftp/dest")
    static final File ARCHIVEREADONLYROOT = new File("${System.getProperty('TESTFSREADROOT')}/src/test/resources/test-archives")
    
    def server
    
    FtpServer() {
        
       def userManagerFactory = new PropertiesUserManagerFactory()
       def userMgr = userManagerFactory.createUserManager()
       def serverFactory = new FtpServerFactory()
       def factory = new ListenerFactory()

       [ 
           'guest'   : TESTFSREADONLYROOT.absolutePath,
           'archive' : ARCHIVEREADONLYROOT.absolutePath,
           'root'    : TESTFSWRITEROOT.absolutePath
       ].each { k,v ->
           def user = new BaseUser()
           def auths = new ArrayList<Authority>()
           def auth = new WritePermission()
           
           auths.add(auth)
           user.setName( k )
           user.setPassword( k )
           user.setHomeDirectory( v )
           user.setAuthorities(auths)
           userMgr.save(user)
       }

       serverFactory.setUserManager( userMgr )
       factory.setPort( PORT )
       serverFactory.addListener("default", factory.createListener())
        
       server = serverFactory.createServer();
    } 
    
    void start() {
        server.start()
    }
    
    void stop() {
        server.stop()
    }
    

}
