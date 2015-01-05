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

package org.ysb33r.groovy.dsl.vfs.impl

import spock.lang.*

import org.ysb33r.groovy.dsl.vfs.FileActionException
import org.apache.commons.vfs2.Selectors
import org.apache.commons.vfs2.VFS
import org.apache.commons.vfs2.FileType
import org.apache.commons.vfs2.AllFileSelector
import org.apache.commons.io.FileUtils
import static org.ysb33r.groovy.dsl.vfs.impl.CopyMoveOperations.move

class MoveOperationsSpec extends Specification {

	@Shared def testFsReadOnlyRoot = new File("${System.getProperty('TESTFSREADROOT')}/src/test/resources/test-files")
	@Shared def testFsReadRoot = new File( "${System.getProperty('TESTFSWRITEROOT') ?: 'build/tmp/test-files'}/impl_move/src")
	@Shared def testFsWriteRoot = new File( "${System.getProperty('TESTFSWRITEROOT') ?: 'build/tmp/test-files'}/impl_move/dest")

    def vfs=VFS.manager

    
    def expectedFiles= ['file1.txt','file2.txt','test-subdir/file3.txt','test-subdir/file4.txt']
	
	def setupSpec  ()  {
		assert testFsReadOnlyRoot.exists()
	}


	def setup() {
		[ testFsReadRoot,testFsWriteRoot ].each {
			if (!it.exists()) {
				it.mkdirs()
			}
		}
        FileUtils.copyDirectory(testFsReadOnlyRoot,testFsReadRoot)
	}

	def cleanup() {
		[ testFsReadRoot,testFsWriteRoot ].each { it.deleteDir() }
	}

    def "Moving file to existing folder, not containing a same-named file"() {
        setup:
            def from=vfs.resolveFile(testFsReadRoot,"${expectedFiles[0]}")
            def to
            
        when: "folder name + file name specified, overwrite, smash, is false"
            to=vfs.resolveFile(testFsWriteRoot,expectedFiles[0])
            move(from,to,false,false)
            
        then: "move file"
            new File("${testFsWriteRoot}/${expectedFiles[0]}").exists()
            new File("${testFsReadRoot}/${expectedFiles[0]}").exists() == false
            
        when: "folder name + different file name specified, overwrite, smash, is false"
            new File("${testFsWriteRoot}/${expectedFiles[0]}").delete()
            from=vfs.resolveFile(testFsReadRoot,"${expectedFiles[1]}")
            to=vfs.resolveFile(testFsWriteRoot,'destination-file.txt')
            move(from,to,false,false)
            
        then: "move file"
            new File("${testFsWriteRoot}/destination-file.txt").exists()
            new File("${testFsReadRoot}/${expectedFiles[1]}").exists() == false
            
        when: "file with only folder name specified, overwrite, smash is false"
            new File("${testFsWriteRoot}/destination-file.txt").delete()
            from=vfs.resolveFile(testFsReadRoot,"${expectedFiles[2]}")
            to=vfs.toFileObject(testFsWriteRoot)
            move(from,to,false,false)
            
        then: "move file"
            new File("${testFsWriteRoot}/file3.txt").exists()
            new File("${testFsReadRoot}/${expectedFiles[2]}").exists() == false
            
    }
    
    def "Moving file to existing folder, not containing a same-named file, but applying smash"() {
        setup:
            def from=vfs.resolveFile(testFsReadRoot,"${expectedFiles[0]}")
            def to
            
        when: "folder name is specified, smash is true"
            def target=new File("${testFsWriteRoot}/${expectedFiles[0]}")
            target.mkdir()
            to=vfs.resolveFile(testFsWriteRoot,expectedFiles[0])
            move(from,to,true,false)
            
        then: "replace folder with file"
            target.exists()
            target.isFile()
            new File("${testFsReadRoot}/${expectedFiles[0]}").exists() == false
            
    }
            
    def "Moving file to existing folder containing a same named file"() {
        setup:
            def source=new File("${testFsReadRoot}/${expectedFiles[0]}")
            def from=vfs.toFileObject(source)
            def target=new File("${testFsWriteRoot}/${expectedFiles[0]}")
            def to=vfs.toFileObject(target)
            FileUtils.copyFile(source, target)
            assert target.exists()
            
        when: "overwrite, smash are all false"
            from=vfs.resolveFile(testFsReadRoot,"${expectedFiles[1]}")
            move(from,to,false,false)
            
        then: "file must not be moved, throw exception"
            thrown(FileActionException)
            target.text == source.text
            
        when: "overwrite is true, but smash is false"
            from=vfs.resolveFile(testFsReadRoot,"${expectedFiles[2]}")
            move(from,to,false,true)

        then: "file must be overwritten"
            target.text == new File("${testFsReadOnlyRoot}/${expectedFiles[2]}").text
            new File("${testFsReadRoot}/${expectedFiles[2]}").exists() == false
            
        when: "smash is true, regardless of other settings"
            from=vfs.resolveFile(testFsReadRoot,"${expectedFiles[3]}")
            move(from,to,true,false)

        then: "replace existing target with file, regardless"
            target.text == new File("${testFsReadOnlyRoot}/${expectedFiles[3]}").text
            new File("${testFsReadRoot}/${expectedFiles[3]}").exists() == false
            
    }
    
    def "Moving folder to non-existing folder, within existing parent folder, no smash"() {
        setup:
            def source=new File("${testFsReadRoot}/test-subdir")
            def from=vfs.toFileObject(source)
            def root=new File("${testFsWriteRoot}")
            def to=vfs.toFileObject(root)
            def expected=new File("${testFsWriteRoot}/test-subdir")
            
        when: "non-existing folder name is specified, overwrite+smash is false"
            move(from,to,false,false)
            
        then: "move folder"
            expected.exists()
            expected.isDirectory()
            new File("${testFsWriteRoot}/${expectedFiles[2]}").exists()
            source.exists() == false
    }
    
        
    def "Moving folder to specifed new non-existing folder, within existing parent folder, no smash"() {
        setup:
            def source=new File("${testFsReadRoot}/test-subdir")
            def from=vfs.toFileObject(source)
            def root=new File("${testFsWriteRoot}/destination-subdir")
            def to=vfs.toFileObject(root)

       when: "target parent folder name is specified, overwrite is false, smash is false, same-named child folder does not exist"
            move(from,to,false,false)
            
       then: "move source folder as child of target folder"
             root.exists()
            root.isDirectory()
            new File("${testFsWriteRoot}/destination-subdir/file3.txt").exists()            
            source.exists() == false
    }
    
    def "Moving folder underneath same parent, no smash"() {
        setup:
            def source=new File("${testFsReadRoot}/test-subdir")
            def dest=new File("${testFsReadRoot}/test-subdir2")
            
        when: "target and source share the same parent, and target folder does not exist"
            move( vfs.toFileObject(source), vfs.toFileObject(dest), false, false)
            
        then: "rename folder"
            source.exists() == false
            dest.exists()
    }
    
    def "Moving folder to existing folder"() {
        setup:
            def source=new File("${testFsReadRoot}/test-subdir")
            def from=vfs.toFileObject(source)
            def root=new File("${testFsWriteRoot}")
            def to=vfs.toFileObject(root)
            def expected= new File("${testFsWriteRoot}/test-subdir")
            expected.mkdir()

        when: "target parent folder name is specified, overwrite is false, smash is false, same-named child folder exists" 
            move(from,to,false,false)    
            
        then: "don't move, throw exception"
            thrown(FileActionException)
            source.exists()
            
        when: "smash is true"
            def toBeDestroyed = new File("${expected}/toBeDestroyed")
            toBeDestroyed.mkdir()
            move(from,vfs.toFileObject(expected),true,false)
         
        then: "replace target folder with source folder, deleting content of target folder"
            source.exists() == false
            toBeDestroyed.exists() == false
            new File("${testFsWriteRoot}/${expectedFiles[3]}").exists()
            expected.exists()
    }
    
    def "Moving folder to existing folder with same-named child folder and have overwrite:true"() {
        setup: "Create an exiting subdirectory containing "
            def source=new File("${testFsReadRoot}")
            def from=vfs.toFileObject(source)
            def root=new File("${testFsWriteRoot}")
            def to=vfs.toFileObject(root)
            
        and: "Create a subfolder below a folder with same name as in source"
            def expected= new File("${testFsWriteRoot}/test-subdir/one-more")
            expected.mkdirs()
            
        and: "Add one file to this subfolder"
            def expectedFile=new File("${expected}/something.txt")
            expectedFile.text = "FOOBAR"
            
        and: "Create a file with the same path as the source"
            new File("${root}/${expectedFiles[2]}").text = "BARFOO"
            
        when: "target parent folder name is specified, overwrite is true, smash is false, same-named child folder exists"
            from.children.each {
                move(it,to,false,true)
            }

        then:
            expected.exists()
            expectedFile.exists()
            expectedFile.text == 'FOOBAR'
            new File("${root}/${expectedFiles[0]}").text == new File("${testFsReadOnlyRoot}/${expectedFiles[0]}").text
            new File("${root}/${expectedFiles[2]}").text == new File("${testFsReadOnlyRoot}/${expectedFiles[2]}").text
    }
    
    def "Moving folder to existing folder with same-named child folder and using an overwrite closure"() {
        setup: 
            def source=new File("${testFsReadRoot}")
            def from=vfs.toFileObject(source)
            def root=new File("${testFsWriteRoot}")
            def to=vfs.toFileObject(root)
            
        and: "Create files with same names as the source"
            def expected= new File("${testFsWriteRoot}/test-subdir")
            expected.mkdirs()
            expectedFiles.each {
                new File("${testFsWriteRoot}/${it}").text = "FOOBAR"
            }
            
        when: "target parent folder name is specified, overwrite is true, smash is false, same-named child folder exists"
            Closure overwrite = { f,t ->
                f.name.baseName.startsWith('file2') || f.name.baseName.startsWith('file4')
            }
            from.children.each {
                move(it,to,false,overwrite)
            }

        then:
            new File("${root}/${expectedFiles[0]}").text == "FOOBAR"
            new File("${root}/${expectedFiles[1]}").text == new File("${testFsReadOnlyRoot}/${expectedFiles[1]}").text
            new File("${root}/${expectedFiles[2]}").text == "FOOBAR"
            new File("${root}/${expectedFiles[3]}").text == new File("${testFsReadOnlyRoot}/${expectedFiles[3]}").text
    }
    
    def "Moving folder to existing file"() {
        setup:
            def source=new File("${testFsReadRoot}/${expectedFiles[0]}")
            def expected= new File("${testFsWriteRoot}/${expectedFiles[0]}")
            def srcDir= new File ("${testFsReadRoot}/test-subdir")
            def root=new File("${testFsWriteRoot}")
            def from=vfs.toFileObject(srcDir)
            def to=vfs.toFileObject( expected )
            
        when: "overwrite is false, smash is false"
            FileUtils.copyFileToDirectory(source,root)
            assert expected.exists()
            move( from,to,false,false )
            
        then: "don't move, throw exception"
            thrown(FileActionException)
            expected.exists()
            expected.isFile()
            srcDir.exists()
            
        when: "overwrite is true, smash is false"
            move( from,to,false,true )

        then: "don't move, throw exception"
            thrown(FileActionException)
            expected.exists()
            expected.isFile()
            srcDir.exists()
            
        when: "smash is true"
            move( from,to,true,false )
        
        then: "replace file with folder"
            expected.exists()
            expected.isDirectory()
            srcDir.exists() == false
            new File("${expected}/file3.txt").exists()
        
    }
    
    def "Moving when intermediate target parent folders do not exist"() {
        setup:
            def source=new File("${testFsReadRoot}/${expectedFiles[0]}")
            def target=new File("${testFsWriteRoot}/A/B/C/${expectedFiles[0]}")
            def from=vfs.toFileObject(source)
            def to=vfs.toFileObject(target)
            
        when: "target intermediates do not exist"
            move(from,to,false,false)
            
        then: "create intermediates during move"
            source.exists() == false
            target.exists()
    }

     def "Moving when intermediate target parent folders do not exist, but intermediates:false"() {
        setup:
            def source=new File("${testFsReadRoot}/${expectedFiles[0]}")
            def target=new File("${testFsWriteRoot}/A/B/C/${expectedFiles[0]}")
            def from=vfs.toFileObject(source)
            def to=vfs.toFileObject(target)
            
        when: "target intermediates do not exist"
            move(from,to,false,false,false)
            
        then: "don't create intermediates"
            source.exists()
            thrown(FileActionException)
    }
}
