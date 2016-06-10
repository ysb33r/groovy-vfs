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
package org.ysb33r.groovy.vfsplugin.cpio

import org.apache.commons.logging.impl.SimpleLog
import org.ysb33r.groovy.dsl.vfs.VFS
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll


class CpioPluginSpec extends Specification {

    static final File TESTFSREADROOT = new File(System.getProperty('TESTFSREADROOT') ?: 'src/test/resources/test-archives')
    static final File TESTFSWRITEROOT = new File( "${System.getProperty('TESTFSWRITEROOT') ?: 'build/tmp'}/cpio" )
    static final File CPIO_INPUT = new File(TESTFSREADROOT,'test-files.cpio').absoluteFile
    static final File CPIO_GZ_INPUT = new File(TESTFSREADROOT,'test-files.cpio.gz').absoluteFile
    static final File CPIO_BZ2_INPUT = new File(TESTFSREADROOT,'test-files.cpio.bz2').absoluteFile

    static final String baseScheme = 'cpio'
    static final String baseUrl = "${baseScheme}://"
//    @Shared String readUrl = "${baseUrl}/${SmbServer.READSHARE}"
//    @Shared String writeUrl = "${baseUrl}/${SmbServer.WRITESHARE}/smb_plugin"
//    @Shared String readDir  = new File(SmbServer.READDIR,'test-files').absoluteFile
    @Shared File   writeDir = TESTFSWRITEROOT
    @Shared VFS vfs


    static final sources = [
        'cpio' : "${baseUrl}${CPIO_INPUT}",
        'cpio.gz': "${baseScheme}gz://${CPIO_GZ_INPUT}",
        'cpio.bz2': "${baseScheme}bz2://${CPIO_BZ2_INPUT}"
    ]

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
                provider className: 'org.ysb33r.groovy.vfsplugin.cpio.CpioFileProvider', schemes: [baseScheme,'cpiogz','cpiobz2']
            }
        }

    }


    void setup() {
        if (writeDir.exists()) {
            assert writeDir.deleteDir()
        }
        writeDir.mkdirs()
    }

    @Unroll
    def "Can we list files in a archive: #name"() {
        given:
        def fNames = []

        vfs.script {
            ls inputArchive, {
                fNames << it.name.toString()
            }
        }

        expect:
        fNames.find { String it -> it.endsWith('file2.txt') }
        fNames.find { String it -> it.endsWith('file1.txt') }
        fNames.find { String it -> it.endsWith('test-subdir') }

        where:
        inputArchive << sources.values()
        name << sources.keySet()
    }

    @Unroll
    def "Copy a file from #name archive to local filesystem"() {
        given:
        def target1=new File(writeDir,'file3.txt')
        def target2=new File(writeDir,'file2.txt')

        vfs.script {
            cp "${inputArchive}!/test-subdir/file3.txt", target1
            cp "${inputArchive}!/file2.txt", target2
        }

        expect:
        target1.exists()
        target1.text.contains('This is test-file #3 in test-subdir')
        target2.exists()
        target2.text.contains('This is test file #2')

        where:
        inputArchive << sources.values()
        name << sources.keySet()
    }

}