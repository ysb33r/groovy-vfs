/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2016
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

// After an idea from http://snipplr.com/view/64953/

package org.ysb33r.vfs.test.services

import org.apache.ftpserver.ConnectionConfigFactory
import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.ftplet.Authority
import org.apache.ftpserver.listener.ListenerFactory
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory
import org.apache.ftpserver.usermanager.impl.BaseUser
import org.apache.ftpserver.usermanager.impl.WritePermission

class FtpServer {

    static final def PORT  = System.getProperty('FTPPORT') ?: 50021   
    static final def READROOT    = "ftp://guest:guest@localhost:${PORT}"
    static final def ARCHIVEROOT = "ftp://archive:archive@localhost:${PORT}"
    static final def WRITEROOT   = "ftp://root:root@localhost:${PORT}"

    def server
    
    FtpServer( File readRoot, File archiveReadRoot, File writeRoot ) {
        
       def userManagerFactory = new PropertiesUserManagerFactory()
       def userMgr = userManagerFactory.createUserManager()
       def serverFactory = new FtpServerFactory()
       def factory = new ListenerFactory()
       def configFactory = new ConnectionConfigFactory()

       [ 
           'guest'   : readRoot.absolutePath,
           'archive' : archiveReadRoot.absolutePath,
           'root'    : writeRoot.absolutePath
       ].each { k,v ->
           def user = new BaseUser()
           ArrayList<Authority> auths = []
           def auth = new WritePermission()
           
           auths.add(auth)
           user.setName( k )
           user.setPassword( k )
           user.setHomeDirectory( v )
           user.setAuthorities(auths)
           userMgr.save(user)
       }

       configFactory.maxLogins = 20
       serverFactory.userManager = userMgr 
       serverFactory.connectionConfig = configFactory.createConnectionConfig()
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
