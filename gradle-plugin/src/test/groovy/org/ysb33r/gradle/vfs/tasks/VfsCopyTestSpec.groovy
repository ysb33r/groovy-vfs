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
                from "${server.READROOT}/file1.txt"
                into "${server.WRITEROOT}/foo"
            }

        when:
            copyTask.exec()

        then:
            new File(TESTFSWRITEROOT,'foo/file1.txt').exists()
    }
}