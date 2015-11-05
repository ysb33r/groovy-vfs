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
import io.milton.servlet.MiltonServlet
//import it.could.webdav.DAVServlet
//import org.apache.jackrabbit.servlet.jackrabbit.JackrabbitRepositoryServlet
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder


//import org.ysb33r.groovy.vfs.test.services.WebdavServer as JackrabbitWebdavServer

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
    static final File TESTFSREADONLYROOT  = new File("${System.getProperty('TESTFSREADROOT')}/src/test/resources/test-files")
    static final File TESTFSWRITEROOT     = new File(SERVERROOT,'write')
    static final File ARCHIVEREADONLYROOT = new File("${System.getProperty('TESTFSREADROOT')}/src/test/resources/test-archives")

    Server server
//    JackrabbitWebdavServer server

    WebdavServer() {

//        server = new JackrabbitWebdavServer()
//        server.repository = SERVERROOT
        server = new Server(PORT)
        ServletContextHandler servletContentHandler = new ServletContextHandler()
        [    read : TESTFSREADONLYROOT,
            write : TESTFSWRITEROOT,
          archive : ARCHIVEREADONLYROOT
        ].each { path,root ->
            ServletHolder servletRegistration = servletContentHandler.addServlet(MiltonServlet,"/${path}")
            servletRegistration.setInitParameter('rootDir',root.absolutePath)
//            ServletHolder servletRegistration = servletContentHandler.addServlet(JackrabbitRepositoryServlet,"/${path}")
//            servletRegistration.setInitParameter('repository.home',root.absolutePath)
//            servletRegistration.setInitParameter('rootPath',root.absolutePath)
        }
        server.handler = servletContentHandler

    }

    void start() {
        server.start()
//        server.run()
    }

    void stop() {
        server.stop()
//        server.shutdown()
    }

}
