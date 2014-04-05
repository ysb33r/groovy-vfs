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

import org.alfresco.jlan.app.JLANCifsServer

class SmbServer implements Runnable {

    static final String CONFIGXML = System.getProperty('JLANCONFIG') ?: 'src/test/resources/jlanserver.xml'
    static final def CONFIG = new XmlSlurper().parse(new File(CONFIGXML))
    static final String HOSTNAME = CONFIG.SMB.host.bindto.text()
    static final String PORT = CONFIG.SMB.host.tcpipSMB.@port
    static final String DOMAIN = CONFIG.SMB.host.@domain
    static final String USER = CONFIG.security.users.user[1].@name
    static final String PASSWORD = CONFIG.security.users.user[1].password.text()
    static final String READSHARE = CONFIG.shares.diskshare[0].@name
    static final String READDIR = new File(CONFIG.shares.diskshare[0].driver.LocalPath.text())
    static final String WRITESHARE = CONFIG.shares.diskshare[1].@name

    def server

    void run() {
            JLANCifsServer.main(CONFIGXML)
    }

    void start() {
        server = new Thread(this).start()
        sleep(2000)
    }
    void stop() {
        JLANCifsServer.shutdownServer(CONFIGXML)
    }
}