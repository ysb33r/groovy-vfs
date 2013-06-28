// ============================================================================
// Copyright (C) Schalk W. Cronje 2012
//
// This software is licensed under the Apche License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
// ============================================================================
package org.ysb33r.groovy.dsl.vfs.impl

import spock.lang.*

import org.ysb33r.groovy.dsl.vfs.FileActionException
import org.apache.commons.vfs2.Selectors
import org.apache.commons.vfs2.VFS
import org.apache.commons.vfs2.FileType
import org.apache.commons.vfs2.AllFileSelector
import org.apache.commons.io.FileUtils

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
            def from=vfs.resolveFile(testFsReadRoot,"test-files/${expectedFiles[0]}")
            def to
            
        when: "folder name + file name specified, overwrite, smash, is false"
            to=vfs.resolveFile(testFsWriteRoot,expectedFiles[0])
            CopyMoveOperations.move(from,to,false,false)
            
        then: "move file"
            new File("${testFsWriteRoot}/${expectedFiles[0]}").exists()
            
        when: "folder name + different file name specified, overwrite, smash, is false"
            new File("${testFsWriteRoot}/${expectedFiles[0]}").delete()
            from=vfs.resolveFile(testFsReadRoot,"test-files/${expectedFiles[1]}")
            to=vfs.resolveFile(testFsWriteRoot,'destination-file.txt')
            CopyMoveOperations.move(from,to,false,false)
            
        then: "move file"
            new File("${testFsWriteRoot}/destination-file.txt").exists()
            
        when: "file with only folder name specified, overwrite, smash is false"
            new File("${testFsWriteRoot}/destination-file.txt").delete()
            from=vfs.resolveFile(testFsReadRoot,"test-files/${expectedFiles[2]}")
            to=vfs.toFileObject(testFsWriteRoot)
            CopyMoveOperations.move(from,to,false,false)
            
        then: "move file"
            new File("${testFsWriteRoot}/file3.txt").exists()
                    
    }
    
    def "Moving file to existing folder, not containing a same-named file, but applying smash"() {
        setup:
            def from=vfs.resolveFile(testFsReadRoot,"test-files/${expectedFiles[0]}")
            def to
            
        when: "folder name is specified, smash is true"
            def target=new File("${testFsWriteRoot}/${expectedFiles[0]}")
            target.mkdir()
            to=vfs.resolveFile(testFsWriteRoot,expectedFiles[0])
            CopyMoveOperations.move(from,to,true,false)
            
        then: "replace folder with file"
            target.exists()
            target.isFile()
    }
            
    def "Moving file to existing folder containing a same named file"() {
        setup:
            def source=new File("${testFsReadRoot}/test-files/${expectedFiles[0]}")
            def from=vfs.toFileObject(source)
            def target=new File("${testFsWriteRoot}/${expectedFiles[0]}")
            def to=vfs.toFileObject(target)
            FileUtils.copyFile(source, target)
            assert target.exists()
            
        when: "overwrite, smash are all false"
            from=vfs.resolveFile(testFsReadRoot,"test-files/${expectedFiles[1]}")
            CopyMoveOperations.move(from,to,false,false)
            
        then: "file must not be moved, throw exception"
            thrown(FileActionException)
            
        when: "overwrite is true, but smash is false"
            from=vfs.resolveFile(testFsReadRoot,"test-files/${expectedFiles[2]}")
            CopyMoveOperations.move(from,to,false,true)

        then: "file must be overwritten"
            target.text == new File("${testFsReadRoot}/test-files/${expectedFiles[2]}").text
            
        when: "smash is true, regardless of other settings"
            from=vfs.resolveFile(testFsReadRoot,"test-files/${expectedFiles[3]}")
            CopyMoveOperations.move(from,to,true,false)

        then: "replace existing target with file, regardless"
            target.text == new File("${testFsReadRoot}/test-files/${expectedFiles[3]}").text
            
    }
    
    @Ignore
    def "Moving folder to non-existing folder, within existing parent folder"() {
        when: "non-existing folder name is specified, overwrite+smash is false"
        
        then: "move folder"
        false
        
        when: "parent folder name is specified, overwrite+smash is false"
        
        then: "move folder within parent folder"
        false
        
        when: "parent folder name is specified, overwrite is true, smash is false"
        
        then: "don't move, throw exception"
        false
        
        when: "target and source share the same parent, and target fodler does not exist"
        
        then: "rename folder"
            false
    }
    
    @Ignore
    def "Moving folder to existing folder"() {
        when: "overwrite+smash is false"
        
        then: "don't move, throw exception"
            false
        
        when: "smash is true"
        
        then: "replace target folder with source folder, deleting content of target folder"
            false
        
        when: "target parent folder name is specified, overwrite is false, smash is false, same-named child folder does not exist"
        
        then: "move source folder as child of target folder"
            false
            
        when: "target parent folder name is specified, overwrite is false, smash is false, same-named child folder exists" 
        
        then: "don't move, throw exception"
            false
    }
    
    @Ignore
    def "Moving folder to existing file"() {
        when: "overwrite is false, smash is false"
        
        then: "don't move, throw exception"
            false
            
        when: "overwrite is true, smash is false"

        then: "don't move, throw exception"
            false
            
        when: "smash is true"
        
        then: "replace file with folder"
            false
        
    }
}
