// ============================================================================
// Copyright (C) Schalk W. Cronje 2012
//
// This software is licensed under the Apche License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
// ============================================================================
package org.ysb33r.groovy.dsl.vfs.impl

import static org.junit.Assert.*
import org.junit.Ignore
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.ysb33r.groovy.dsl.vfs.FileActionException
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS
import org.apache.commons.vfs2.FileType
import org.apache.commons.vfs2.AllFileSelector

class TestMoveOperations {

	static def testFsReadOnlyRoot = new File("${System.getProperty('TESTFSREADROOT')}/src/test/resources/test-files")
	static def testFsReadRoot
	static def testFsWriteRoot
	
	static def expectedFiles= ['file1.txt','file2.txt','test-subdir/file3.txt','test-subdir/file4.txt']
	
	@BeforeClass
	static void createFileStructure()  {
		assert testFsReadOnlyRoot.exists()
		testFsReadRoot= new File( "${System.getProperty('TESTFSWRITEROOT') ?: 'build/tmp/test-files'}/impl_move/src")
		testFsWriteRoot= new File( "${System.getProperty('TESTFSWRITEROOT') ?: 'build/tmp/test-files'}/impl_move/dest")

	}

	@AfterClass
	static void tearDownAfterClass() throws Exception {
	}

	@Before
	void setUp() {
		[ testFsReadRoot,testFsWriteRoot ].each {
			if (!it.exists()) {
				it.mkdirs()
			}
		}
		CopyMoveOperations.copy()
	}

	@After
	void tearDown() {
		[ testFsReadRoot,testFsWriteRoot ].each { it.deleteDir() }
	}

	// ------------------------------------------------------------------------
	// File -> Directory, No Filter
	// ------------------------------------------------------------------------
	@Test
	@Ignore
	void MoveFileToNonExistingTargetWithiExistingParentTree_MovesFile() {
		def vfs=VFS.manager
		def from=vfs.resolveFile(testFsReadOnlyRoot,expectedFiles[0])
		def to=vfs.resolveFile(testFsWriteRoot,'destination-file1.txt')

		fail "NOT IMPLEMENTED"		

	}

	 /* <tr>
	 *   <td>FILE</td>  <td>IMAGINARY</td><td>No</td> <td>No</td> <td>Create new file, delete old file</td>
	 * </tr><tr>
	 *   <td>FILE</td>  <td>FILE</td>     <td>No</td> <td>No</td> <td>Don't move</td>
	 * </tr><tr>
	 *   <td>FILE</td>  <td>FILE</td>     <td>Yes</td><td>No</td> <tdOverwirte existing file with source, delete old file></td>
	 * </tr><tr>
	 *   <td>FILE</td>  <td>FOLDER</td>   <td>No</td> <td>No</td> <td>Move file into folder except if same-name target file exists</td>
	 * </tr><tr>
	 *   <td>FILE</td>  <td>FOLDER</td>   <td>Yes</td><td>No</td> <td>Move file into folder, replacing any existing same-name target file</td>
	 * </tr><tr>
	 *   <td>FILE</td>  <td>FOLDER</td>   <td>-</td>  <td>Yes</td><td>Replace fodler with file</td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>IMAGINARY</td><td>No</td> <td>No</td> <td>Create new folder with content. Delete old folder</td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FILE</td>     <td>No</td> <td>No</td> <td>Don't move</td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FILE</td>     <td>Yes</td><td>No</td> <td>Don't move</td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FILE</td>     <td>-</td>  <td>Yes</td><td>Replace file with folder</td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FOLDER</td>   <td>No</td> <td>No</td> <td>Move folder as a sub-folder of destination. Fails if same-name target exists</td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FOLDER</td>   <td>Yes</td><td>No</td> <td>Move folder as a sub-folder of destination. Fails is same-name target exists and not empty.</td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FOLDER</td>   <td>--</td> <td>Yes</td><td>Delete old folder. Move source folder in place.</td>
	 * </tr>
**/
}
