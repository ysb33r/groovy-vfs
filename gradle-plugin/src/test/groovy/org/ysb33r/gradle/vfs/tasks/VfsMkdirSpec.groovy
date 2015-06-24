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
package org.ysb33r.gradle.vfs.tasks

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.ysb33r.groovy.vfs.test.services.FtpServer
import spock.lang.Shared
import spock.lang.Specification


/**
 * Created by schalkc on 01/01/15.
 */
class VfsMkdirSpec extends Specification {

    static final File TESTFSREADONLYROOT  = new File("${System.getProperty('TESTFSREADROOT')}/src/test/resources/test-files")
    static final File TESTFSWRITEROOT     = new File("${System.getProperty('TESTFSWRITEROOT') ?: 'build/tmp/test-files'}/ftp/dest")
    static final File ARCHIVEREADONLYROOT = new File("${System.getProperty('TESTFSREADROOT')}/src/test/resources/test-archives")

    @Shared FtpServer server

    Project project = ProjectBuilder.builder().build()
    Task vfsTask = project.tasks.create( 'fooTask', VfsMkdir )

    void setupSpec() {
        server = new FtpServer(TESTFSREADONLYROOT,ARCHIVEREADONLYROOT,TESTFSWRITEROOT)
        server.start()
    }

    void cleanupSpec() {
        server.stop()
    }

    void setup() {
        if(TESTFSWRITEROOT.exists()) {
            TESTFSWRITEROOT.deleteDir()
        }
        TESTFSWRITEROOT.mkdirs()
    }

    def "Create remote directory" () {
        given: "We would like to create two sets of directories"
        vfsTask.configure {
            directories "${FtpServer.WRITEROOT}/file1/file2","${FtpServer.WRITEROOT}/file3/file4"

            praxis intermediates : true
        }

        when: "The task is executed"
        vfsTask.exec()

        then: "We expect those directories to have been created on the remote server"
        new File(TESTFSWRITEROOT,'file1/file2').isDirectory()
        new File(TESTFSWRITEROOT,'file3/file4').isDirectory()


    }

    def "Up to date is purely based upon whether directory exists" () {
        when: "We would like to create a directory, without execution"
        vfsTask.configure {
            directories "${FtpServer.WRITEROOT}/file1/file2"
        }

        then: "The task is not up to date"
        !vfsTask.isUpToDate()

        when: "The directory does exist, but the task has not yet executed"
        new File(TESTFSWRITEROOT,'file1/file2').mkdirs()

        then: "The task is up to date"
        !vfsTask.isUpToDate()

        when: "Only one directory out of a collection does not exist"
        vfsTask.configure {
            directories "${FtpServer.WRITEROOT}/file3/file4"
        }

        then: "The task is not up to date"
        !vfsTask.isUpToDate()
    }
}