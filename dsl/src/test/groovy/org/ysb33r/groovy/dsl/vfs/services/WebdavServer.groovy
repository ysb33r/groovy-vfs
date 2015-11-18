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
package org.ysb33r.groovy.dsl.vfs.services

import groovy.transform.CompileStatic
import org.apache.commons.io.FileUtils

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
class WebdavServer {

    static final Integer PORT  = System.getProperty('WEBDAVPORT')?.toInteger() ?: 50081
    static final String READROOT    = "webdav://guest:guest@localhost:${PORT}/read"
    static final String ARCHIVEROOT = "webdav://archive:archive@localhost:${PORT}/archives"
    static final String WRITEROOT   = "webdav://root:root@localhost:${PORT}/upload"
    static final File SERVERROOT = new File("${System.getProperty('TESTFSWRITEROOT') ?: 'build/tmp/test-files'}/webdav")
    static final File TESTFSREADONLYROOT  = new File("${System.getProperty('TESTFSREADROOT') ?: '.'}/src/test/resources/test-files")
    static final File TESTFSWRITEROOT     = new File(SERVERROOT,'write')
    static final File ARCHIVEREADONLYROOT = new File("${System.getProperty('TESTFSREADROOT') ?: '.'}/src/test/resources/test-archives")

    org.ysb33r.groovy.vfs.test.services.WebdavServer server

    WebdavServer() {
        ['read','archives','write'].each { new File(SERVERROOT,it).mkdirs() }
        FileUtils.copyDirectory(TESTFSREADONLYROOT,new File(SERVERROOT,'read'))
        FileUtils.copyDirectory(ARCHIVEREADONLYROOT,new File(SERVERROOT,'archives'))

        server = new org.ysb33r.groovy.vfs.test.services.WebdavServer(
            homeFolder :  SERVERROOT,
            port : PORT
        )
    }

    void start() {
        server.start()
    }

    void stop() {
        server.stop()
    }

}
