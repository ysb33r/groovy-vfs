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

	// ------------------------------------------------------------------------
	// File -> Directory, No Filter
	// ------------------------------------------------------------------------
	@Test
	void copyFileToExistingDirectory_AddsToDirectoryIfNonExistingFileNameSupplied() {
		def vfs=VFS.manager
		def from=vfs.resolveFile(testFsReadOnlyRoot,expectedFiles[0])
		def to=vfs.resolveFile(testFsWriteRoot,'destination-file1.txt')
		
		assertTrue "Source file must exist, otherwise unit test is useless",from.exists()

		CopyMoveOperations.copy(from,to,false,false,false)
		
		assertTrue new File("${testFsWriteRoot}/destination-file1.txt") .exists()
		assertFalse new File("${testFsWriteRoot}/${expectedFiles[0]}") .exists()
	}
	
	@Test(expected=FileActionException)
	void copyFileToExistingDirectory_OverwriteOff_FailsIfTargetFileExists() {
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
	void copyFileToExistingDirectory_OverwriteOn_ReplacesIfTargetFileExists() {
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
	void copyFileToExistingDirectory_OverwriteOff_FailsIfItContainsDirectoryWithSameNameAsSource() {
		def vfs=VFS.manager
		def from=vfs.resolveFile(testFsReadOnlyRoot,expectedFiles[0])
		def to=vfs.toFileObject(testFsWriteRoot)
		def sameNameDir=new File("${testFsWriteRoot}/${expectedFiles[0]}")
		sameNameDir.mkdirs()
		CopyMoveOperations.copy(from,to,false,false,false)
	}

	@Test(expected=FileActionException)
	void copyFileToExistingDirectory_OverwriteOn_FailsIfItContainsDirectoryWithSameNameAsSource() {
		def vfs=VFS.manager
		def from=vfs.resolveFile(testFsReadOnlyRoot,expectedFiles[0])
		def to=vfs.toFileObject(testFsWriteRoot)
		def sameNameDir=new File("${testFsWriteRoot}/${expectedFiles[0]}")
		sameNameDir.mkdirs()
		CopyMoveOperations.copy(from,to,false,true,false)		
	}

	@Test
	void copyFileToExistingDirectory_OverwriteOff_AddsToDirectory() {
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
	void copyFileToExistingDirectoryWithSameName_OverwriteOnSmashOff_AddsToDirectory() {
		def vfs=VFS.manager
		def from=vfs.resolveFile(testFsReadOnlyRoot,expectedFiles[0])
		def sameNameDir=new File("${testFsWriteRoot}/${expectedFiles[0]}")
		sameNameDir.mkdirs()
		def to=vfs.toFileObject(sameNameDir)
		CopyMoveOperations.copy(from,to,false,true,false)
		assertTrue "Expected to see file '${testFsWriteRoot}/${expectedFiles[0]}/${expectedFiles[0]}'",new File("${testFsWriteRoot}/${expectedFiles[0]}/${expectedFiles[0]}") .exists()
	}

	@Test
	void copyFileToExistingDirectory_SmashOn_ReplacesDirectoryWithFile() {
		def vfs=VFS.manager
		def from=vfs.resolveFile(testFsReadOnlyRoot,expectedFiles[0])
		def sameNameDir=new File("${testFsWriteRoot}/${expectedFiles[0]}")
		sameNameDir.mkdirs()
		def to=vfs.toFileObject(sameNameDir)
		assertEquals "Before copy operation destination is a folder",FileType.FOLDER,to.type
		
		CopyMoveOperations.copy(from,to,true,true,false)
		assertEquals "After copy operation destination should be a file if smash=true",FileType.FILE,to.type
		
	}

	// ------------------------------------------------------------------------
	// Directory -> Directory, No Filter
	// ------------------------------------------------------------------------
	@Test(expected=FileActionException)
	void copyDirectoryToExistingDirectory_RecursiveOffOverwriteOffSmashOff_Fails() {
		def vfs=VFS.manager
		def from=vfs.toFileObject(testFsReadOnlyRoot)
		def to=vfs.toFileObject(testFsWriteRoot)
		def targetDir="${testFsWriteRoot}/${from.name.baseName}"

		CopyMoveOperations.copy(from,to,false,false,false)
	}
	
	@Test(expected=FileActionException)
	void copyDirectoryToExistingDirectory_RecursiveOffOverwriteOnSmashOff_Fails() {
		def vfs=VFS.manager
		def from=vfs.toFileObject(testFsReadOnlyRoot)
		def to=vfs.toFileObject(testFsWriteRoot)
		def targetDir="${testFsWriteRoot}/${from.name.baseName}"

		CopyMoveOperations.copy(from,to,false,true,false)
	}
	
	@Test
	void copyDirectoryToNewTarget_RecursiveOnSmashOff_CreatesDirectoryAndCopiesTree() {
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
	void copyDirectoryToNewTargetWIthIntermediatDirectories_RecursiveOnSmashOff_CreatesDirectoryIncludingIntermediatesAndCopiesTree() {
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
	void copyDirectoryToExistingDirectory_RecursiveOnOverwriteOffSmashOff_AddsSubfolder() {
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


	// TODO: Like to update this test to include deeper directory copies
	@Test
	void copyDirectoryToExistingDirectoryWithSameNamedSubfolder_RecursiveOnOverwriteOffSmashOff_PopulatesSubFolder() {
		def vfs=VFS.manager
		def from=vfs.toFileObject(new File("${testFsReadOnlyRoot}/test-subdir"))
		def to=vfs.toFileObject(testFsWriteRoot)
		def targetDir="${testFsWriteRoot}/test-subdir"
		new File(targetDir).mkdirs()

		assertTrue "Target dir '${to}' must exist prior to testing copy function",to.exists()
		
		println "Copying from folder '${from}' to existing folder '${to}'"
		println "Expecting to see file[34].txt to appear in ${to.resolveFile('test-subdir')}"

		CopyMoveOperations.copy(from,to,false,false,true)
		
		(3..4) .each {
			def expected=to.resolveFile("test-subdir/file${it}.txt")
			assertTrue "Expected ${targetDir}/file${it}.txt",expected.exists()
			assertTrue "Expected ${targetDir}/file${it}.txt",new File("${targetDir}/file${it}.txt").exists()
			assertEquals FileType.FILE,expected.type
			
			// The reason for this next assertion is a bug in the code also 
			// caused duplication of file[34] in the parent directory
			expected=to.resolveFile("file${it}.txt")
			assertFalse "Don't want ${targetDir}/../file${it}.txt",expected.exists()
		}
	}

	@Test(expected=FileActionException)
	void copyDirectoryToExistingDirectoryWithSameNamedSubfolderAndSomeSameNameFiles_RecursiveOnOverwriteOffSmashOff_PopulatesSubfolderButFailsOnFirstSameNamedChild() {
		def vfs=VFS.manager
		def from=vfs.toFileObject(new File("${testFsReadOnlyRoot}/test-subdir"))
		def to=vfs.toFileObject(testFsWriteRoot)
		def targetDir="${testFsWriteRoot}/test-subdir"
		new File(targetDir).mkdirs()

		assertTrue "Target dir '${to}' must exist prior to testing copy function",to.exists()

		// Copy first time should pass
		CopyMoveOperations.copy(from,to,false,false,true)
		def expected=to.resolveFile("test-subdir/file3.txt")
		assertTrue "Expected ${targetDir}/file3.txt",expected.exists()

		// Second copy should fail, because destination files will now exist 
		CopyMoveOperations.copy(from,to,false,false,true)		
	}

	@Test
	void copyDirectoryToExistingDirectory_RecursiveOnOverwriteOnSmashOff_AddsSubfolder() {
		def vfs=VFS.manager
		def from=vfs.toFileObject(testFsReadOnlyRoot)
		def to=vfs.toFileObject(testFsWriteRoot)
		def targetDir="${testFsWriteRoot}/${from.name.baseName}"
		
		println "Copying from folder '${from}' to folder '${to}'"
		println "Final result should be '${to}/${from.name.baseName}"
		assertFalse "Target directory must not exist at this point",new File(targetDir).exists()
		
		CopyMoveOperations.copy(from,to,false,true,true)
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
	void copyDirectoryToExistingDirectorySameNamedSubfolder_RecursiveOnOverwriteOnSmashOff_PopulatesSubfolderOverwritingExistingFiles() {
		fail "NOT IMPLEMENTED"
	}

	@Ignore
	@Test(expected=FileActionException)
	void copyDirectoryToExistingDirectoryWithSameNamedSubfolder_RecursiveOverwriteOnSmashOff_PopulatesSubfolderOverwritingFilesButFailsIfSourceIsFailAndDestinationIsFolder() {
		fail "NOT IMPLEMENTED"
		
	}
	

	// ------------------------------------------------------------------------
	// Directory -> Directory/File, With Smash, No Filter
	// ------------------------------------------------------------------------
	@Ignore
	@Test
	void copyDirectoryToExistingFile_SmashOn_WithReplaceFileWithFolder() {
		fail "NOT IMPLEMENTED"		
	}
	
	@Ignore
	@Test
	void copyDirectoryToExistingDirectory_SmashOn_WillAddSubfolder() {
		fail "NOT IMPLEMENTED"		
	}
	
	@Ignore
	@Test
	void copyDirectoryToExistingDirectory_SmashOn_WillSmashSameNamedChildFile() {
		fail "NOT IMPLEMENTED"		
	}
	
	@Ignore
	@Test
	void copyDirectoryToExistingDirectory_SmashOn_WillSmashSameNamedChildFolder() {
		fail "NOT IMPLEMENTED"		
	}
	
	// ------------------------------------------------------------------------
	// File -> Directory, With Filter
	// ------------------------------------------------------------------------
	@Ignore
	@Test
	void copyFileWithFilterToExistingDirectory_AddsToDirectoryIfNonExistingFileNameSuppliedAndFilterMatches() {
		fail "NOT IMPLEMENTED - FilterDoesNotMatch"		
		fail "NOT IMPLEMENTED - FilterMatches"		
	}

	@Ignore
	@Test(expected=FileActionException)
	void copyFileWithFilterToExistingDirectory_OverwriteOff_FailsIfTargetFileExistsAndFileMatches() {
		fail "NOT IMPLEMENTED - FilterDoesNotMatch"		
		fail "NOT IMPLEMENTED - FilterMatches"		
	}

	@Ignore
	@Test
	void copyFileWithFilterToExistingDirectory_OverwriteOn_ReplacesIfTargetFileExists() {
		fail "NOT IMPLEMENTED - FilterDoesNotMatch"		
		fail "NOT IMPLEMENTED - FilterMatches"		
	}

	// ------------------------------------------------------------------------
	// Directory -> Directory, With Filter
	// ------------------------------------------------------------------------

}
