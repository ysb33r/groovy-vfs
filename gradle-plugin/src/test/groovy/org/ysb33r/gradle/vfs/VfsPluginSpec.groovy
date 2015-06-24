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
// ============================================================================
// (C) Copyright Schalk W. Cronje 2013
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================

package org.ysb33r.gradle.vfs

import java.io.File;

import spock.lang.*
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder
import org.apache.commons.vfs2.FileSystemOptions
import org.ysb33r.groovy.dsl.vfs.URI

class VfsPluginSpec extends spock.lang.Specification {
    File testFsReadOnlyRoot = new File("${System.getProperty('TESTFSREADROOT')}/src/test/resources/test-files")
    String testFsURI = new URI(testFsReadOnlyRoot).toString()
    File testFsWriteRoot= new File( "${System.getProperty('TESTFSWRITEROOT') ?: 'build/tmp/test-files'}/file")
    String testFsWriteURI= new URI(testFsWriteRoot).toString()
    Project project = ProjectBuilder.builder().build()
    
    
    def "Can apply Vfs plugin to project"() {
        given:
            project.apply plugin:'org.ysb33r.vfs'
        
        expect:
            project.__vfs instanceof org.ysb33r.groovy.dsl.vfs.VFS
    }
    
    def "Must be able to set vfs style properties via a configuration-style block"() {
        FtpFileSystemConfigBuilder fscb
        
        given:
            project.apply plugin:'org.ysb33r.vfs'
            fscb = project.__vfs.fsMgr.getFileSystemConfigBuilder('ftp') as FtpFileSystemConfigBuilder
            fscb.setPassiveMode( project.__vfs.defaultFSOptions, false)
            project.vfs {
                    options {
                            ftp {
                                passiveMode true
                            }
                    }
            }

        expect:
            fscb.getPassiveMode( project.__vfs.defaultFSOptions )
    }
    
    def "Calling VFS closures should execute immediately"() {
        
        Integer count= 0
        given:
            testFsWriteRoot.deleteDir()
            project.apply plugin:'org.ysb33r.vfs'
            project.vfs {
                cp testFsURI,"${testFsWriteURI}/one/two/three", recursive:true
            }
            testFsWriteRoot.eachFileRecurse {
                if (it.name =~ /file\d\.txt/ ) { 
                    ++count 
                }
            }
            
        expect:
          count == 4
        
    }
}

