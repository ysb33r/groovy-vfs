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
import org.ysb33r.groovy.dsl.vfs.FileActionException;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS
import org.apache.commons.vfs2.FileType
import org.apache.commons.vfs2.AllFileSelector

class TestCopyMoveOperations {

	static def testFsReadOnlyRoot = new File('src/test/resources/test-files')
	static def testFsWriteRoot
	
	static def expectedFiles= ['file1.txt','file2.txt']

	@BeforeClass
	static void createFileStructure()  {
		assert testFsReadOnlyRoot.exists()
		testFsWriteRoot= new File( "${System.getProperty('TESTFSWRITEROOT') ?: 'build/tmp/test-files'}/copymove")

	}

	@AfterClass
	static void tearDownAfterClass() throws Exception {
	}

	@Before
	void setUp() {
		if (!testFsWriteRoot.exists()) {
			testFsWriteRoot.mkdirs()
		}
	}

	@After
	void tearDown() {
		testFsWriteRoot.deleteDir()
	}

	@Test
	void copyFileToExistingDirectoryAddsToDirectoryIfNonExistingFileNameSupplied() {
		def vfs=VFS.manager
		def from=vfs.resolveFile(testFsReadOnlyRoot,expectedFiles[0])
		def to=vfs.resolveFile(testFsWriteRoot,'destination-file1.txt')
		
		assertTrue "Source file must exist, otherwise unit test is useless",from.exists()

		CopyMoveOperations.copy(from,to,false,false)
		
		assertTrue new File("${testFsWriteRoot}/destination-file1.txt") .exists()
		assertFalse new File("${testFsWriteRoot}/${expectedFiles[0]}") .exists()
	}
	
	@Test(expected=FileActionException)
	void copyFileToExistingDirectoryFailsIfTargetFileExistsAndOverwriteIsOff() {
		def vfs=VFS.manager
		def from=vfs.resolveFile(testFsReadOnlyRoot,expectedFiles[0])
		def to=vfs.resolveFile(testFsWriteRoot,'destination-file1.txt')
		
		assertTrue "Source file must exist, otherwise unit test is useless",from.exists()

		CopyMoveOperations.copy(from,to,false,false)
		assertTrue "First write should have succeeded", new File("${testFsWriteRoot}/destination-file1.txt") .exists()
		
		// Should cause an exception
		CopyMoveOperations.copy(from,to,false,false)
	}
	
	@Test
	void copyFileToExistingDirectoryReplacesIfTargetFileExistsAndOverwriteIsOn() {
		def vfs=VFS.manager
		def from=vfs.resolveFile(testFsReadOnlyRoot,expectedFiles[0])
		def to=vfs.resolveFile(testFsWriteRoot,'destination-file1.txt')
		
		assertTrue "Source file must exist, otherwise unit test is useless",from.exists()

		CopyMoveOperations.copy(from,to,false,false)
		assertTrue "First write should have succeeded", new File("${testFsWriteRoot}/destination-file1.txt") .exists()

		from=vfs.resolveFile(testFsReadOnlyRoot,expectedFiles[1])
		CopyMoveOperations.copy(from,to,false,true)
		assertTrue "Second write should have succeeded", new File("${testFsWriteRoot}/destination-file1.txt") .exists()		
		assertEquals "Content should reflect that of new file",from.content.inputStream.text,to.content.inputStream.text
	}

	@Test(expected=FileActionException)
	void copyFileToExistingDirectoryFailsIfItContainsDirectoryWithSameNameAsSourceWithOverwriteOff() {
		def vfs=VFS.manager
		def from=vfs.resolveFile(testFsReadOnlyRoot,expectedFiles[0])
		def to=vfs.toFileObject(testFsWriteRoot)
		def sameNameDir=new File("${testFsWriteRoot}/${expectedFiles[0]}")
		sameNameDir.mkdirs()
		CopyMoveOperations.copy(from,to,false,false)
	}

	@Test(expected=FileActionException)
	void copyFileToExistingDirectoryFailsIfItContainsDirectoryWithSameNameAsSourceWithOverwriteOn() {
		def vfs=VFS.manager
		def from=vfs.resolveFile(testFsReadOnlyRoot,expectedFiles[0])
		def to=vfs.toFileObject(testFsWriteRoot)
		def sameNameDir=new File("${testFsWriteRoot}/${expectedFiles[0]}")
		sameNameDir.mkdirs()
		CopyMoveOperations.copy(from,to,false,true)		
	}

	@Test
	void copyFileToExistingDirectoryAddsToDirectory() {
		def vfs=VFS.manager
		def from=vfs.toFileObject(testFsReadOnlyRoot).resolveFile(expectedFiles[0])
		def to=vfs.toFileObject(testFsWriteRoot)

		assertTrue "Source file must exist, otherwise unit test is useless",from.exists()
		assertTrue "Dest dir must exist, otherwise unit test is useless",to.exists()
		
		CopyMoveOperations.copy(from,to,false,false)

		assertTrue "Expected target files to have been copied with same name",new File("${testFsWriteRoot}/${expectedFiles[0]}") .exists()
		assertFalse "Expected no other files to be copied",new File("${testFsWriteRoot}/${expectedFiles[1]}") .exists()
	}

	@Test
	void copyFileToExistingDirectoryWithSameNameAddsToDirectoryIfSmashOffAndOverwriteOn() {
		def vfs=VFS.manager
		def from=vfs.resolveFile(testFsReadOnlyRoot,expectedFiles[0])
		def sameNameDir=new File("${testFsWriteRoot}/${expectedFiles[0]}")
		sameNameDir.mkdirs()
		def to=vfs.toFileObject(sameNameDir)
		CopyMoveOperations.copy(from,to,false,true)
		assertTrue "Expected to see file '${testFsWriteRoot}/${expectedFiles[0]}/${expectedFiles[0]}'",new File("${testFsWriteRoot}/${expectedFiles[0]}/${expectedFiles[0]}") .exists()
	}

	@Test
	void copyFileToExistingDirectoryReplacesDirectoryWithFileIfSmashOn() {
		def vfs=VFS.manager
		def from=vfs.resolveFile(testFsReadOnlyRoot,expectedFiles[0])
		def sameNameDir=new File("${testFsWriteRoot}/${expectedFiles[0]}")
		sameNameDir.mkdirs()
		def to=vfs.toFileObject(sameNameDir)
		assertEquals "Before copy operation destination is a folder",FileType.FOLDER,to.type
		
		CopyMoveOperations.copy(from,to,true,true)
		assertEquals "After copy operation destination should be a file if smash=true",FileType.FILE,to.type
		
	}

}
