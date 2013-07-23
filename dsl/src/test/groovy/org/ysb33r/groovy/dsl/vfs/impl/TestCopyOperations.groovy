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

class TestCopyOperations {

	static def testFsReadOnlyRoot = new File("${System.getProperty('TESTFSREADROOT')}/src/test/resources/test-files")
	static def testFsWriteRoot
	
	static def expectedFiles= ['file1.txt','file2.txt','test-subdir/file3.txt','test-subdir/file4.txt']
	
	@BeforeClass
	static void createFileStructure()  {
		assert testFsReadOnlyRoot.exists()
		testFsWriteRoot= new File( "${System.getProperty('TESTFSWRITEROOT') ?: 'build/tmp/test-files'}/impl_copy")

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

    @Test
    void treeCopyOverExistingTree_RecursiveOnOverwriteOnSmashOff_OverwritesFiles() {
        def vfs=VFS.manager
        def from=vfs.toFileObject(testFsReadOnlyRoot)
        def to=vfs.toFileObject(testFsWriteRoot)
        def targetDir="${testFsWriteRoot}"

        assertTrue "Target dir '${to}' must exist prior to testing copy function",to.exists()
 
        new File("${testFsWriteRoot}/test-files/test-subdir").mkdirs()
        expectedFiles.each {
            new File("${testFsWriteRoot}/test-files/${it}").text = '1234'
        }
        CopyMoveOperations.copy(from,to,false,true,true)

        expectedFiles.each {
            def file = new File("${testFsWriteRoot}/test-files/${it}")
            assertTrue "Expected ${file}",file.exists()
            assertEquals "Expected content to have changed for ${file}", new File("${testFsReadOnlyRoot}/${it}").text, file.text
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
	
	@Test
	void copyDirectoryToExistingDirectorySameNamedSubfolder_RecursiveOnOverwriteOnSmashOff_PopulatesSubfolderOverwritingExistingFiles() {
		def vfs=VFS.manager
		def srcDir=new File("${testFsReadOnlyRoot}/test-subdir")
		def from=vfs.toFileObject(srcDir)
		def to=vfs.toFileObject(testFsWriteRoot)
		def targetDir="${testFsWriteRoot}/test-subdir"
		new File(targetDir).mkdirs()

		assertTrue "Target dir '${to}' must exist prior to testing copy function",to.exists()

		CopyMoveOperations.copy(from,to,false,true,true)
		def expected=to.resolveFile("test-subdir/file3.txt")
		assertTrue "Expected ${targetDir}/file3.txt",expected.exists()

		(3..4).each {
			def f=new File("${targetDir}/file${it}.txt")
			assertTrue new File("${targetDir}/file${it}.txt").text == new File("${srcDir}/file${it}.txt").text 
			f.text = "${it}"
			assertFalse f.text == new File("${srcDir}/file${it}.txt").text 
		}
		
		CopyMoveOperations.copy(from,to,false,true,true)
		(3..4).each {
			assertTrue new File("${targetDir}/file${it}.txt").text == new File("${srcDir}/file${it}.txt").text 
		}
	}

	@Test(expected=FileActionException)
	void copyDirectoryToExistingDirectoryWithSameNamedSubfolder_RecursiveOverwriteOnSmashOff_PopulatesSubfolderOverwritingFilesButFailsIfSourceIsFileAndDestinationIsFolder() {
		def vfs=VFS.manager
		def srcDir=new File("${testFsReadOnlyRoot}/test-subdir")
		def from=vfs.toFileObject(srcDir)
		def to=vfs.toFileObject(testFsWriteRoot)
		def targetDir="${testFsWriteRoot}/test-subdir"
		new File("${targetDir}/file4.txt").mkdirs()

		CopyMoveOperations.copy(from,to,false,true,true)
	}
	

	// ------------------------------------------------------------------------
	// Directory -> File, No Smash, No Filter
	// ------------------------------------------------------------------------
	@Test(expected=FileActionException)
	void copyDirectoryToExistingFile_SmashOffOverwriteOnRecursiveOn_WillFail() {
		def vfs=VFS.manager
		def srcDir=new File("${testFsReadOnlyRoot}/test-subdir")
		def targetDir="${testFsWriteRoot}/test-subdir"
		new File(targetDir).text="FOO"
		def from=vfs.toFileObject(srcDir)
		def to=vfs.toFileObject(testFsWriteRoot)

		CopyMoveOperations.copy(from,to,false,true,true)
	}

	// ------------------------------------------------------------------------
	// Directory -> Directory/File, With Smash, No Filter
	// ------------------------------------------------------------------------
	@Test
	void copyDirectoryToExistingFile_SmashOn_WithReplaceFileWithFolder() {
		def vfs=VFS.manager
		def srcDir=new File("${testFsReadOnlyRoot}/test-subdir")
		def target="${testFsWriteRoot}/test-subdir"
		new File(target).text="FOO"
		def from=vfs.toFileObject(srcDir)
		def to=vfs.toFileObject(new File(target))

		assertEquals "Expects target to be a file before performing copy",FileType.FILE,to.type
		
		CopyMoveOperations.copy(from,to,true,false,false)
		
		assertEquals "Expects target to be a folder after performing copy",FileType.FOLDER,to.type
	}
	
	@Test
	void copyDirectoryToExistingDirectory_SmashOn_WillDeleteTargetFolderAndCopySourceFolder() {
		def vfs=VFS.manager
		def srcDir=new File("${testFsReadOnlyRoot}")
		def targetDir=new File("${testFsWriteRoot}/do_not_smash_me")
		def from=vfs.toFileObject(srcDir)
		def to=vfs.toFileObject(targetDir)
		
		println """Smash copy from '${CopyMoveOperations.friendlyURI(from)}' 
                   to '${CopyMoveOperations.friendlyURI(to)}' should result 
                   in '${to.parent.resolveFile(from.name.baseName)}'"""
		
		targetDir.mkdirs()
		assertTrue "Before copying, ensure that the target folder exists",to.exists()
		
		CopyMoveOperations.copy(from,to,true,false,false)
		to=vfs.toFileObject(new File("${testFsWriteRoot}/${from.name.baseName}"))
		assertTrue "After copying, the target folder should have the same base name as the source folder", to.exists()
		assertEquals FileType.FOLDER,to.type
		
		from.children.each {
			def expected=to.resolveFile(it.name.baseName)
			assertTrue "Expected to see ${CopyMoveOperations.friendlyURI(expected)} in target folder",expected.exists()
		}
	}
	
	// ------------------------------------------------------------------------
	// File -> Directory, With Filter
	// ------------------------------------------------------------------------
	@Test
	void copyFileWithPatternFilterToExistingDirectory_AddsToDirectoryIfNonExistingFileNameSuppliedAndFilterMatches() {
		def vfs=VFS.manager
		def from=vfs.toFileObject(new File("${testFsReadOnlyRoot}/file2.txt"))
		def to=vfs.toFileObject(testFsWriteRoot)

		CopyMoveOperations.copy(from,to,false,false,false,/file1\.txt/)
		assertFalse new File("${testFsWriteRoot}/file2.txt").exists()
		assertFalse new File("${testFsWriteRoot}/file1.txt").exists()
	
		CopyMoveOperations.copy(from,to,false,false,false,~/file2\.txt/)
		assertTrue new File("${testFsWriteRoot}/file2.txt").exists()
		assertFalse new File("${testFsWriteRoot}/file1.txt").exists()
	}

	@Test(expected=FileActionException)
	void copyFileWithPatternToExistingDirectory_OverwriteOff_FailsIfTargetFileExistsAndFileMatches() {
		def vfs=VFS.manager
		def from=vfs.toFileObject(new File("${testFsReadOnlyRoot}/file2.txt"))
		def to=vfs.toFileObject(testFsWriteRoot)

		CopyMoveOperations.copy(from,to,false,false,false)
		assertTrue "The target file must exist, before running the filter test",
			new File("${testFsWriteRoot}/file2.txt").exists()
		
		CopyMoveOperations.copy(from,to,false,false,false,/file1\.txt/)
		assertFalse new File("${testFsWriteRoot}/file2.txt").exists()
	
		CopyMoveOperations.copy(from,to,false,false,false,~/file2\.txt/)
	}

	@Test
	void copyFileWithPatternToExistingDirectory_OverwriteOn_ReplacesIfTargetFileExists() {
		def vfs=VFS.manager
		def srcFile=new File("${testFsReadOnlyRoot}/file2.txt")
		def from=vfs.toFileObject(srcFile)
		def to=vfs.toFileObject(testFsWriteRoot)

		CopyMoveOperations.copy(from,to,false,false,false)
		def fileUnderTest=new File("${testFsWriteRoot}/file2.txt")
		assertTrue "The target file must exist, before running the filter test",
			fileUnderTest.exists()
		fileUnderTest.text="FOOBAR"
		assertNotEquals srcFile.text,fileUnderTest.text
		
		CopyMoveOperations.copy(from,to,false,true,false,/file1\.txt/)
		assertNotEquals "If the filter does nto match, then file must not be overwritten",
			srcFile.text,fileUnderTest.text
	
		CopyMoveOperations.copy(from,to,false,true,false,~/file2\.txt/)
		assertEquals srcFile.text,fileUnderTest.text
}

	// TODO: What about using a closure as a filter?
	// TODO: What about using with a normal selector from VFS
	
	// ------------------------------------------------------------------------
	// Directory -> Directory, With Filter
	// ------------------------------------------------------------------------

	// ------------------------------------------------------------------------
	// Miscellaneous tests
	// ------------------------------------------------------------------------
	@Test(expected=FileActionException)
	void copyNonExistingSourceRaisesException() {
		def vfs=VFS.manager
		def from=vfs.toFileObject(new File("${testFsReadOnlyRoot}/non_existing_file"))
		def to=vfs.toFileObject(testFsWriteRoot)

		CopyMoveOperations.copy(from,to,false,false,false)
	}
}
