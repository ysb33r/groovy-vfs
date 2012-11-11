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

class TestCopyMoveOperations {

	static def testFsReadOnlyRoot = new File('src/test/resources/test-files')
	static def testFsWriteRoot
	
	static def expectedFiles= ['file1.txt','file2.txt','test-subdir/file3.txt','test-subdir/file4.txt']
	
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

		CopyMoveOperations.copy(from,to,false,false,false)
		
		assertTrue new File("${testFsWriteRoot}/destination-file1.txt") .exists()
		assertFalse new File("${testFsWriteRoot}/${expectedFiles[0]}") .exists()
	}
	
	@Test(expected=FileActionException)
	void copyFileToExistingDirectoryFailsIfTargetFileExistsAndOverwriteIsOff() {
		def vfs=VFS.manager
		def from=vfs.resolveFile(testFsReadOnlyRoot,expectedFiles[0])
		def to=vfs.resolveFile(testFsWriteRoot,'destination-file1.txt')
		
		assertTrue "Source file must exist, otherwise unit test is useless",from.exists()

		CopyMoveOperations.copy(from,to,false,false,false)
		assertTrue "First write should have succeeded", new File("${testFsWriteRoot}/destination-file1.txt") .exists()
		
		// Should cause an exception
		CopyMoveOperations.copy(from,to,false,false,false)
	}
	
	@Test
	void copyFileToExistingDirectoryReplacesIfTargetFileExistsAndOverwriteIsOn() {
		def vfs=VFS.manager
		def from=vfs.resolveFile(testFsReadOnlyRoot,expectedFiles[0])
		def to=vfs.resolveFile(testFsWriteRoot,'destination-file1.txt')
		
		assertTrue "Source file must exist, otherwise unit test is useless",from.exists()

		CopyMoveOperations.copy(from,to,false,false,false)
		assertTrue "First write should have succeeded", new File("${testFsWriteRoot}/destination-file1.txt") .exists()

		from=vfs.resolveFile(testFsReadOnlyRoot,expectedFiles[1])
		CopyMoveOperations.copy(from,to,false,true,false)
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
		CopyMoveOperations.copy(from,to,false,false,false)
	}

	@Test(expected=FileActionException)
	void copyFileToExistingDirectoryFailsIfItContainsDirectoryWithSameNameAsSourceWithOverwriteOn() {
		def vfs=VFS.manager
		def from=vfs.resolveFile(testFsReadOnlyRoot,expectedFiles[0])
		def to=vfs.toFileObject(testFsWriteRoot)
		def sameNameDir=new File("${testFsWriteRoot}/${expectedFiles[0]}")
		sameNameDir.mkdirs()
		CopyMoveOperations.copy(from,to,false,true,false)		
	}

	@Test
	void copyFileToExistingDirectoryAddsToDirectory() {
		def vfs=VFS.manager
		def from=vfs.toFileObject(testFsReadOnlyRoot).resolveFile(expectedFiles[0])
		def to=vfs.toFileObject(testFsWriteRoot)

		assertTrue "Source file must exist, otherwise unit test is useless",from.exists()
		assertTrue "Dest dir must exist, otherwise unit test is useless",to.exists()
		
		CopyMoveOperations.copy(from,to,false,false,false)

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
		CopyMoveOperations.copy(from,to,false,true,false)
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
		
		CopyMoveOperations.copy(from,to,true,true,false)
		assertEquals "After copy operation destination should be a file if smash=true",FileType.FILE,to.type
		
	}

	@Test(expected=FileActionException)
	void copyDirectoryToExistingDirectoryShouldFailIfNoRecursiveNoOverwriteNoSmash() {
		def vfs=VFS.manager
		def from=vfs.toFileObject(testFsReadOnlyRoot)
		def to=vfs.toFileObject(testFsWriteRoot)
		def targetDir="${testFsWriteRoot}/${from.name.baseName}"

		CopyMoveOperations.copy(from,to,false,false,false)
	}
	
	@Test
	void copyDirectoryToNewTargetCreatesDirectoryAndCopiesTreeIfRecursive() {
		def vfs=VFS.manager
		def from=vfs.toFileObject(testFsReadOnlyRoot)
		def to=vfs.toFileObject(testFsWriteRoot).resolveFile("destination-dir")
		def targetDir="${testFsWriteRoot}/destination-dir"

		println "Copying from folder '${from}' to folder '${to}'"
		println "Final result should be '${to} because it currently does not exist"
		assertFalse "Target directory must not exist at this point",new File(targetDir).exists()
		
		CopyMoveOperations.copy(from,to,false,false,true)
				
		expectedFiles.each {
			assertTrue "Expected '${targetDir}/${it}'", new File("${targetDir}/${it}").exists()
			assertEquals FileType.FILE,to.resolveFile(it).type
		}
	}
	
	@Test
	void copyDirectoryToNewTargetCreatesDirectoryIncludingIntermediatesAndCopiesTreeIfRecursive() {
		def vfs=VFS.manager
		def from=vfs.toFileObject(testFsReadOnlyRoot)
		def to=vfs.toFileObject(testFsWriteRoot).resolveFile("intermediate-dir/destination-dir")
		def targetDir="${testFsWriteRoot}/intermediate-dir/destination-dir"

		println "Copying from folder '${from}' to folder '${to}'"
		println "Final result should be '${to} because it, and its intermediates, currently do not exist"
		assertFalse "Target directory must not exist at this point",new File(targetDir).exists()
		
		CopyMoveOperations.copy(from,to,false,false,true)
				
		expectedFiles.each {
			assertTrue "Expected '${targetDir}/${it}'", new File("${targetDir}/${it}").exists()
			assertEquals FileType.FILE,to.resolveFile(it).type
		}
	}
	
	@Test
	void copyDirectoryToExistingDirectoryAddsSubfolderIfRecursiveNoOverwriteNoSmash() {
		def vfs=VFS.manager
		def from=vfs.toFileObject(testFsReadOnlyRoot)
		def to=vfs.toFileObject(testFsWriteRoot)
		def targetDir="${testFsWriteRoot}/${from.name.baseName}"
		
		println "Copying from folder '${from}' to folder '${to}'"
		println "Final result should be '${to}/${from.name.baseName}"
		assertFalse "Target directory must not exist at this point",new File(targetDir).exists()
		
		CopyMoveOperations.copy(from,to,false,false,true)
		assertTrue "Target directory should exist",new File(targetDir).exists()
		
		expectedFiles.each {
			def expected=to.resolveFile(from.name.baseName).resolveFile(it)
			assertTrue "Expected '${targetDir}/${it}'", expected.exists()
			assertTrue "Expected '${targetDir}/${it}'", new File("${targetDir}/${it}").exists()
			assertEquals FileType.FILE,expected.type
		}

	}

	@Ignore
	@Test
	void copyDirectoryToExistingDirectoryWithSameNamedSubfolderPopulatesSubFolderIfRecursiveNoOverwriteNoSmash() {
		fail "NOT IMPLEMENTED"
	}

	@Ignore
	@Test
	void copyDirectoryToExistingDirectoryWithSameNamedSubfolderPopulatesSubFolderFailsOnFirstSameNamedItemIfRecursiveNoOverwriteNoSmash() {
		fail "NOT IMPLEMENTED"
	}

		// void copyDirectoryToExistingDirectoryFailsIfOverwriteOffAndSameNameChildExists()
	// void copyDirectoryToExistingDirectoryAddsToChildIfOverwriteOnAndSameNameChildDirectoryExists()
	// void copyDirectoryToExistingDirectoryFailsIfOverwriteOnAndSameNameChildFileExists()
	// void copyDirectoryToExistingDirectoryReplacesTargetIfSmashOn()
	// void copyDirectoryOverExistingFileFailsIfSmashOff()
	// void copyDirectoryOverExistingFileReplacesTargetIfSmashOn()
	// More tests with filters 
}
