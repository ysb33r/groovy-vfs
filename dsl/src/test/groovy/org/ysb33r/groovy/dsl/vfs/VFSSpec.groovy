package org.ysb33r.groovy.dsl.vfs

import spock.lang.Specification


/**
 * Created by schalkc on 12/12/14.
 */
class VFSSpec extends Specification {

    static final File testFsReadOnlyRoot = new File("${System.getProperty('TESTFSREADROOT')}/src/test/resources/test-files")
    static final String testFsURI = new URI(testFsReadOnlyRoot).toString()
    static final File testFsWriteRoot = new File( "${System.getProperty('TESTFSWRITEROOT') ?: 'build/tmp/test-files'}/file")
    static final String testFsWriteURI = new URI(testFsWriteRoot).toString()

    VFS vfs

    void setup() {
        vfs=new VFS()

        if(testFsWriteRoot.exists()) {
            testFsWriteRoot.deleteDir()
        }
        testFsWriteRoot.mkdirs()
    }

    def "Char sequences must convert to URI instances"() {
        given:
          def result
          vfs {
              result = uri 'sftp://user:pass@server/dir?vfs.sftp.userDirIsRoot=1'
          }

        expect:
            result.properties.sftp.userDirIsRoot == '1'
            result.toString() == 'sftp://user:pass@server/dir'
    }

    def "Sending text to remote file must update file"() {

        given:
            File dest = new File(testFsWriteRoot,'echo-file.txt')

        when: 'we tell it to overwrite the file'
            vfs {
                overwrite dest with 'test text'
            }

        then: 'we expect the file to be created with the text'
            dest.text == 'test text'

        when: 'we send extra text with an intent to append'
            vfs {
                append dest with ' more text'
            }

        then: 'we want to the text to be added to the end of the file'
            dest.text == 'test text more text'
    }

    def "Sending text to remote file via a stream must update file"() {

        given:
            File dest = new File(testFsWriteRoot,'echo-file.txt')

        when: 'we tell it to overwrite the file'
            vfs {
                overwrite dest, { it << 'test text' }
            }

        then: 'we expect the file to be created with the text'
            dest.text == 'test text'

        when: 'we send extra text with an intent to append'
            vfs {
                append dest, { it << ' more text' }
            }

        then: 'we want to the text to be added to the end of the file'
            dest.text == 'test text more text'
    }

    def "Text cannot be sent to a remote directory"() {
        when:
            vfs {
                overwrite testFsWriteRoot, { it << 'test text' }
            }
        then:
            thrown(FileActionException)
    }

}