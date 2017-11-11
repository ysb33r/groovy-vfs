/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2017
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
package org.ysb33r.vfs.dsl.groovy

import org.ysb33r.vfs.core.VfsURI
import org.ysb33r.vfs.dsl.groovy.helpers.GroovyDslBaseSpecification
import spock.lang.PendingFeature


class VFSSpec extends GroovyDslBaseSpecification {

    static final File testFsReadOnlyRoot = GroovyDslBaseSpecification.testFsReadOnlyRoot
    static final File testFsWriteRoot = GroovyDslBaseSpecification.testFsWriteRoot

    Vfs vfs = setupVfs()

    void "Char sequences must convert to URI instances"() {
        given:
        VfsURI result

        when:
        vfs {
            result = resolveURI 'sftp://user:pass@server/dir?vfs.sftp.userDirIsRoot=1'
        }

        then:
        result.properties.sftp.userDirIsRoot == '1'
        result.toString() == 'sftp://user:pass@server/dir'
    }

    @PendingFeature
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

    @PendingFeature
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

    @PendingFeature
    def "Text cannot be sent to a remote directory"() {
        when:
            vfs {
                overwrite testFsWriteRoot, { it << 'test text' }
            }
        then:
            thrown(FileActionException)
    }

    @PendingFeature
    def "Copying with an ANT-style filter (excludeSelf==true)"() {
        when:
        vfs {
            cp testFsReadOnlyRoot,testFsWriteRoot,
                overwrite:true, recursive:true,
                filter : antPattern {
                    include '**'
                    exclude 'file2.txt'
                    exclude '**/file4.txt'
                }

        }
        then:
        new File(testFsWriteRoot,'file1.txt').exists()
        new File(testFsWriteRoot,'test-subdir/file3.txt').exists()
        !new File(testFsWriteRoot,'file2.txt').exists()
        !new File(testFsWriteRoot,'test-subdir/file4.txt').exists()

    }

    @PendingFeature
    def "Copying with an ANT-style filter (excludeSelf==true) and non-existing target"() {
        given:
        File target = new File(testFsWriteRoot,'destination')

        when:
        vfs {
            cp testFsReadOnlyRoot,target,
                overwrite:true, recursive:true,
                filter : antPattern {
                    include '**'
                    exclude 'file2.txt'
                    exclude '**/file4.txt'
                }

        }
        then:
        new File(target,'file1.txt').exists()
        new File(target,'test-subdir/file3.txt').exists()
        !new File(target,'file2.txt').exists()
        !new File(target,'test-subdir/file4.txt').exists()

    }

    @PendingFeature
    def "Copying with an ANT-style filter (excludeSelf==false)"() {
        when:
        vfs {
            cp testFsReadOnlyRoot,testFsWriteRoot,
                overwrite:true, recursive:true,
                filter : antPattern {
                    include '**'
                    exclude 'file2.txt'
                    exclude '**/file4.txt'
                    excludeSelf = false
                }

        }
        then:
        new File(testFsWriteRoot,'test-files/file1.txt').exists()
        new File(testFsWriteRoot,'test-files/test-subdir/file3.txt').exists()
        !new File(testFsWriteRoot,'test-files/file2.txt').exists()
        !new File(testFsWriteRoot,'test-files/test-subdir/file4.txt').exists()

    }

}