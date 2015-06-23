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
import org.ysb33r.gradle.vfs.VfsPlugin
import org.ysb33r.groovy.vfs.test.services.FtpServer
import spock.lang.Shared
import spock.lang.Specification


/**
 * @author Schalk W. Cronjé
 */
class VfsCopyTestSpec extends Specification {
    static final File TESTFSREADONLYROOT  = new File("${System.getProperty('TESTFSREADROOT')}/src/test/resources/test-files")
    static final File TESTFSWRITEROOT     = new File("${System.getProperty('TESTFSWRITEROOT') ?: 'build/tmp/test-files'}/ftp/dest")
    static final File ARCHIVEREADONLYROOT = new File("${System.getProperty('TESTFSREADROOT')}/src/test/resources/test-archives")

    @Shared FtpServer server

    Project project
    Task copyTask

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
        project = ProjectBuilder.builder().build()
        project.apply plugin : VfsPlugin
        copyTask = project.tasks.create( 'fooTask', VfsCopy )
    }

    boolean exists(final String name) {
        new File(TESTFSWRITEROOT,name).exists()
    }

    boolean not_copied(final String name) {
        !exists(name)
    }

    def "Check praxis for unconfigured task"() {
        expect:
            copyTask.praxis == [ overwrite : true, smash : false, recursive : true ]

    }

    def "If praxis is used, default praxis will not be used"() {
        given:
            copyTask.configure {
                praxis overwrite : true
            }

        expect:
            copyTask.praxis == [ overwrite : true ]
    }

    def "About setting options"() {
        when:
        copyTask.configure {
            options 'vfs.ftp.passiveMode' : true
        }

        then:
            copyTask.getOptions() == [ 'vfs.ftp.passiveMode' : true]

        when:
            copyTask.configure {
                setOptions 'vfs.ftp.userDirIsRoot' : false
            }

        then:
            copyTask.getOptions() == [ 'vfs.ftp.userDirIsRoot' : false ]

        when:
            copyTask.configure {
                options 'vfs.ftp.passiveMode' : true
            }

        then:
            copyTask.getOptions() == [ 'vfs.ftp.userDirIsRoot' : false, 'vfs.ftp.passiveMode' : true ]

    }

    def "Must be able to copy one file across to a remote directory"() {
        given:
            copyTask.configure {
                from "${server.READROOT}", {
                    include 'file1.txt'
                }
                into "${server.WRITEROOT}/foo"
            }

        when:
            copyTask.exec()

        then:
            new File(TESTFSWRITEROOT,'foo/file1.txt').exists()
    }

    def "Must not be able to copy one file across to a remote directory if the lone file is specified in 'from'"() {
        given:
        copyTask.configure {
            from "${server.READROOT}/file1.txt"
            into "${server.WRITEROOT}/foo"
        }

        when:
        copyTask.exec()

        then:
        not_copied 'foo/file1.txt'
    }

    def "Must be able to copy a directory across to a remote directory"() {
        given:
            copyTask.configure {
                from "${server.READROOT}/"
                into "${server.WRITEROOT}/foo"
            }

        when:
            copyTask.exec()

        then:
            new File(TESTFSWRITEROOT,'foo/file1.txt').exists()
            new File(TESTFSWRITEROOT,'foo/file2.txt').exists()
            new File(TESTFSWRITEROOT,'foo/test-subdir/file3.txt').exists()
            new File(TESTFSWRITEROOT,'foo/test-subdir/file4.txt').exists()
    }

    def "Must be able to copy multiple files using a filter across to a remote directory"() {
        given:
        copyTask.configure {
                from "${server.READROOT}", {
                    options filter : ~/file[1]\.txt/
                }
                into "${server.WRITEROOT}/foo"
        }

        when:
            copyTask.exec()

        then:
            exists 'foo/file1.txt'
            not_copied 'foo/file2.txt'
    }

    def "Must be able to copy multiple files using child copy specs"() {
        given:
        def childSpec = project.vfsCopySpec {
            from "${server.READROOT}/test-subdir", {
                exclude 'file4.txt'
            }
            into 'bar'

        }
        copyTask.configure {
            from "${server.READROOT}", {
                include 'file1.txt'
            }
            into "${server.WRITEROOT}/foo"

            with(childSpec)
        }

        when:
        copyTask.exec()

        then:
        exists 'foo/file1.txt'
        not_copied 'foo/file2.txt'
        not_copied 'foo/test-subdir'
        exists 'foo/bar/file3.txt'
        not_copied 'foo/bar/file4.txt'

    }
}