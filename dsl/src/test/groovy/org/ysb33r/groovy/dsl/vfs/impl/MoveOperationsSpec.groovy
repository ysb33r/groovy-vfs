// ============================================================================
// Copyright (C) Schalk W. Cronje 2012
//
// This software is licensed under the Apche License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
// ============================================================================
package org.ysb33r.groovy.dsl.vfs.impl

import spock.lang.*

import org.ysb33r.groovy.dsl.vfs.FileActionException
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS
import org.apache.commons.vfs2.FileType
import org.apache.commons.vfs2.AllFileSelector

class MoveOperationsSpec extends Specification {

	@Shared def testFsReadOnlyRoot = new File("${System.getProperty('TESTFSREADROOT')}/src/test/resources/test-files")
	@Shared def testFsReadRoot = new File( "${System.getProperty('TESTFSWRITEROOT') ?: 'build/tmp/test-files'}/impl_move/src")
	@Shared def testFsWriteRoot = new File( "${System.getProperty('TESTFSWRITEROOT') ?: 'build/tmp/test-files'}/impl_move/dest")

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
        // Here, we believe that 'copy' is actually working :)
		CopyMoveOperations.copy(
            resolveURI([:],testFsReadOnlyRoot),
            resolveURI({:],testFsReadRoot),false,true,true
         )
	}

	def cleanup() {
		[ testFsReadRoot,testFsWriteRoot ].each { it.deleteDir() }
	}

    def "Moving file to existing folder, not containing a same-named file"() {
        when: "folder name specified, overwrite, smash, is false"
            CopyMoveOperation.move
        then: "move file"
            false
            
        when: "file with folder name-specified, overwrite, smash is false"
        
        then: "move file"
            false
            
        when: "folder name is specified, smash is true"
        
        then: "replace folder with file"
            false
    }
    
    def "Moving file to existing folder containing a same named file"() {
        when: "overwrite,smash,recursive are all false"
        
        then: "file must not be moved, throw exception"
            false
            
        when: "overwrite is true, but smash, recursive is false"
        
        then: "file must be moved into subfolder"
            false
            
        when: "smash is true, regardless of other settings"
        
        then: "replace existing target with file, regardless"
            false
            
    }
    
    def "Moving folder to non-existing folder, within existing parent folder"() {
        when: "non-existing folder name is specified, overwrite+smash is false"
        
        then: "move folder"
        false
        
        when "parent folder name is specified, overwrite+smash is false"
        
        then: "move folder within parent folder"
        false
        
        when "parent folder name is specified, overwrite is true, smash is false"
        
        then: "don't move, throw exception"
        false
        
        when: "target and source share the same parent, and target fodler does not exist"
        
        then: "rename folder"
            false
    }
    
    def "Moving folder to existing folder"() {
        when: "overwrite+smash is false"
        
        then: "don't move, throw exception"
            false
        
        when "smash is true"
        
        then: "replace target folder with source folder, deleting content of target folder"
            false
        
        when "parent folder name is specified, overwrite is true, smash is false"
        
        then: "don't move, throw exception"
            false
    }
    
    def "Moving folder to existing file"() {
        when "overwrite is false, smash is false"
        
        then: "don't move, throw exception"
            false
            
        when "overwrite is true, smash is false"

        then: "don't move, throw exception"
            false
            
        when: "smash is true"
        
        then: "replace file with folder"
            false
        
    }
}
