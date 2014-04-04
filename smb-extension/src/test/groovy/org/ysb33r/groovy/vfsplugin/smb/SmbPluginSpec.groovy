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
package org.ysb33r.groovy.vfsplugin.smb

import org.apache.commons.logging.impl.SimpleLog
import spock.lang.*
import org.ysb33r.groovy.dsl.vfs.VFS


class SmbPluginSpec extends Specification {

    @Shared String baseScheme = 'foo'
    @Shared String baseUrl = "${baseScheme}://${SmbServer.DOMAIN}%5C${SmbServer.USER}:${SmbServer.PASSWORD}@${SmbServer.HOSTNAME}:${SmbServer.PORT}"
    @Shared String readUrl = "${baseUrl}/${SmbServer.READSHARE}"
    @Shared String writeUrl = "${baseUrl}/${SmbServer.WRITESHARE}/smb_plugin"
    @Shared File   writeDir = new File( "${System.getProperty('TESTFSWRITEROOT') ?: 'build/tmp/test-files'}/smb_plugin")
    @Shared VFS vfs
    @Shared SmbServer server = new SmbServer()

    void setupSpec() {
        def simpleLog = new SimpleLog(this.class.name)
        simpleLog.setLevel(SimpleLog.LOG_LEVEL_ALL)
        vfs = new VFS(
                logger: simpleLog,
                ignoreDefaultProviders: true
        )

        vfs.script {
            extend {
                provider className: 'org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider', schemes : [ 'file']
                provider className: 'org.ysb33r.groovy.vfsplugin.smb.SmbFileProvider', schemes: [baseScheme]
            }
        }

        server.start()
    }

    void cleanupSpec() {
        server.stop()
    }

    void setup() {
        writeDir.deleteDir()
    }

    void "Can we list files"() {
        given:
            def fNames = []

        when:
            vfs.script {
                ls ("${readUrl}/test-files") {
                    fNames << it.name.toString()
                }
            }

        then:
            fNames.find { String it -> it.endsWith('file2.txt') }
            fNames.find { String it -> it.endsWith('file1.txt') }
            fNames.find { String it -> it.endsWith('test-subdir') }
            fNames[0].find('normal')
    }

    void "We must be able to create a directory on the SMB server"() {
        when:
            vfs.script {
                mkdir "${writeUrl}"
            }

        then:
            writeDir.exists()
    }

    void "Copy a file from one SMB server to another"() {
        when:
        vfs.script {
            cp "${readUrl}/test-files/file2.txt", "${writeUrl}/file2.txt"
        }

        then:
            new File(writeDir,'file2.txt').exists()
    }
}