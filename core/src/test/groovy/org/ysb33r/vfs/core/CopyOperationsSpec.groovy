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
package org.ysb33r.vfs.core

import org.junit.*
import org.ysb33r.vfs.core.helpers.CoreBaseSpecification
import org.ysb33r.vfs.core.helpers.VfsEngineBuilder
import org.ysb33r.vfs.core.impl.CopyMoveOperations
import org.ysb33r.vfs.dsl.groovy.FileActionException
import org.ysb33r.vfs.dsl.groovy.FileType
import spock.lang.PendingFeature

import static org.junit.Assert.*

class CopyOperationsSpec extends CoreBaseSpecification {

	static final File testFsReadOnlyRoot = CoreBaseSpecification.testFsReadOnlyRoot
	static final File testFsWriteRoot = CoreBaseSpecification.identifyWriteRoot('CopyOperationsSpec')
    static final VfsURI readRootURI = new VfsURI(testFsReadOnlyRoot)
    static final VfsURI writeRootURI = new VfsURI(testFsWriteRoot)

	static final List expectedFiles= ['file1.txt','file2.txt','test-subdir/file3.txt','test-subdir/file4.txt']

	VfsEngine vfs = setupVfs()

	void setup() {
		assert testFsReadOnlyRoot.exists()
		if(testFsWriteRoot.exists()) {
			testFsWriteRoot.deleteDir()
		}
        testFsWriteRoot.mkdirs()
        expectedFiles.each {
            assert new File(testFsReadOnlyRoot,it).exists()
        }
	}

	// ------------------------------------------------------------------------
	// File -> Directory, No Filter
	// ------------------------------------------------------------------------
	void 'Copying a file to existing directory adds to directory if non-existing filename is supplied'() {

        given:
		VfsURI from=vfs.resolveURI( readRootURI,expectedFiles[0])
        VfsURI to=vfs.resolveURI(writeRootURI,'destination-file1.txt')

        when:
		CopyMoveOperations.copy(from,to,false,false,false)

        then:
		exists"${testFsWriteRoot}/destination-file1.txt"
		exists "${testFsWriteRoot}/${expectedFiles[0]}"
	}
	
	void 'Copying a file to existing directory with no overwrite set, will fail if target files exists'() {

        given:
        VfsURI from=vfs.resolveURI( readRootURI,expectedFiles[0])
        VfsURI to=vfs.resolveURI(writeRootURI,'destination-file1.txt')

        when:
		CopyMoveOperations.copy(from,to,false,false,false)

        then: "First write will succeed"
		exists"${testFsWriteRoot}/destination-file1.txt"

        when:
		CopyMoveOperations.copy(from,to,false,false,false)

        then: "First write will fail"
        thrown(FileActionException)
	}
	
	void 'Copying a file to existing directory with overwrite set will replace target file if it exists'() {
        given:
        VfsURI from=vfs.resolveURI( readRootURI,expectedFiles[0])
        VfsURI to=vfs.resolveURI(writeRootURI,'destination-file1.txt')

        when:
        CopyMoveOperations.copy(from,to,false,false,false)

        then: 'First write will succeed'
		exists"${testFsWriteRoot}/destination-file1.txt"

        when:
		from=vfs.resolveURI(testFsReadOnlyRoot,expectedFiles[1])
		CopyMoveOperations.copy(from,to,false,true,false)

        then: 'Second write will succeed'
		exists"${testFsWriteRoot}/destination-file1.txt"

        and: 'Content will reflect that of the new file'
		new File(testFsReadOnlyRoot,expectedFiles[1]).text == new File("${testFsWriteRoot}/destination-file1.txt").text
	}

	void 'Copying file to existing directory with overwrite not set will fail if it contains a directory with the same name as the source'() {
        given:
        VfsURI from = vfs.resolveURI( readRootURI,expectedFiles[0])
        VfsURI to = new VfsURI(testFsWriteRoot)
		File sameNameDir=new File("${testFsWriteRoot}/${expectedFiles[0]}")
		sameNameDir.mkdirs()

        when:
		CopyMoveOperations.copy(from,to,false,false,false)

        then:
        thrown(FileActionException)
	}

	void 'Copying a file to existing directory with overwrite on will fail if it contains a directory with the same name as the source'() {
        given:
        VfsURI from = vfs.resolveURI( readRootURI,expectedFiles[0])
        VfsURI to = new VfsURI(testFsWriteRoot)
        File sameNameDir=new File("${testFsWriteRoot}/${expectedFiles[0]}")
        sameNameDir.mkdirs()

        when:
		CopyMoveOperations.copy(from,to,false,true,false)

        then:
        thrown(FileActionException)
	}

	void 'Copying file to existing directory with overwrite off adds file to the directory'() {
        given:
        VfsURI from = vfs.resolveURI( readRootURI,expectedFiles[0])
        VfsURI to = new VfsURI(testFsWriteRoot)

        when:
        CopyMoveOperations.copy(from,to,false,false,false)

        then: "Expected target files will be copied with same name"
		exists"${testFsWriteRoot}/${expectedFiles[0]}"

        and: 'No other files will be copied'
		exists"${testFsWriteRoot}/${expectedFiles[1]}"
	}

	void 'Copying file to existing directory with same name, and with overwrite on and smash off, will add to directory'() {
        given:
        VfsURI from = vfs.resolveURI( readRootURI,expectedFiles[0])
		File sameNameDir=new File("${testFsWriteRoot}/${expectedFiles[0]}")
		VfsURI to= new VfsURI(sameNameDir)

        when:
        sameNameDir.mkdirs()
		CopyMoveOperations.copy(from,to,false,true,false)

        then: 'Expect to see file'
        exists "${testFsWriteRoot}/${expectedFiles[0]}/${expectedFiles[0]}"
	}

	void 'Copying file to existing directory with smash on with replace directory with file'() {
        given:
        VfsURI from = vfs.resolveURI( readRootURI,expectedFiles[0])
        File sameNameDir=new File("${testFsWriteRoot}/${expectedFiles[0]}")

        when:
		sameNameDir.mkdirs()
		VfsURI to = new VfsURI(sameNameDir)

        then:
		vfs.isFolder(to)

        when:
		CopyMoveOperations.copy(from,to,true,true,false)

        then: 'After copy operation, destination is a file'
        vfs.isFile(to)

	}

	// ------------------------------------------------------------------------
	// Directory -> Directory, No Filter
	// ------------------------------------------------------------------------
	void 'Copying directory to existing directory with no recursive, smash or overwrite will fail'() {
        given:
        final VfsURI from = new VfsURI(testFsReadOnlyRoot)
        final VfsURI to = new VfsURI(testFsWriteRoot)

        when:
		CopyMoveOperations.copy(from,to,false,false,false)

        then:
        thrown(FileActionException)
	}
	
	void 'Copying directory to existing directory with no recursive or smash and overwrite set, will fail'() {
        given:
        final VfsURI from = new VfsURI(testFsReadOnlyRoot)
        final VfsURI to = new VfsURI(testFsWriteRoot)

        when:
		CopyMoveOperations.copy(from,to,false,true,false)

        then:
        thrown(FileActionException)
	}
	
	void 'Copying directory to new target with recursive set and smash off will create directory and copy tree'() {
        given:
        final VfsURI from = readRootURI
		final VfsURI to = writeRootURI.resolve('destination-dir')
		final String targetDir="${testFsWriteRoot}/destination-dir"
        assert !new File(targetDir).exists()

        when: 'Copying from source folder to destination folder'
        CopyMoveOperations.copy(from,to,false,false,true)

        then:
		expectedFiles.every {
            exists "${targetDir}/${it}" &&
                new File("${targetDir}/${it}").isFile()
		}
	}
	
	void 'Copying directory to new target with intermediate directories with rescursive on and smash off, will create directory and all intermediaries, and copy tree'() {
        given:
        final VfsURI from = readRootURI
        final VfsURI to = writeRootURI.resolve('intermediate-dir/destination-dir')
        final String targetDir="${testFsWriteRoot}/intermediate-dir/destination-dir"
        assert !new File(targetDir).exists()

        when:
		CopyMoveOperations.copy(from,to,false,false,true)

        then:
        expectedFiles.every {
            exists "${targetDir}/${it}" &&
                new File("${targetDir}/${it}").isFile()
        }
	}
	
	void 'Copying directory to existing directory with recursive on and no overwrite or smash, will add subfolder'() {
        given:
        final VfsURI from = readRootURI
        final VfsURI to = writeRootURI.resolve('destination-dir')
		final String targetDir="${testFsWriteRoot}/${from.name}"
		
		println "Copying from folder '${from}' to folder '${to}'"
		println "Final result should be '${to}/${from.name}"
		assertFalse "Target directory must not exist at this point",new File(targetDir).exists()

        when:
		CopyMoveOperations.copy(from,to,false,false,true)

        then: "Target directory should exist"
        exists targetDir

        and:
		expectedFiles.every {
            VfsURI expected = to.resolve(it)
            exists expected && vfs.isFile(expected)
		}

	}

	// TODO: Like to update this test to include deeper directory copies
	void 'Copying directory to existing directory which contains a same-named subfolder with recurisve set and overwrite + smash off, will populate subfolder'() {
        given:
        final VfsURI from = readRootURI.resolve('test-subdir')
        final VfsURI to = writeRootURI
		final String targetDir = "${testFsWriteRoot}/test-subdir"

        when: 'Copying from source to existing subfolder'
		new File(targetDir).mkdirs()
        CopyMoveOperations.copy(from,to,false,false,true)
        VfsURI expected = to.resolve("test-subdir/file${fileNumber}.txt")

        then: "Target file will appear in subfolder"
        exists expected.path.toFile()
        exists new File("${targetDir}/file${fileNumber}.txt")
        vfs.isFile(expected)

        and: "The file will not appear in the parent folder"
        // The reason for this next assertion is that a bug in the code also
        // caused duplication of file[34] in the parent directory
        ! exists( "${targetDir}/../file${it}.txt" )

        where:
        fileNumber << [3,4]

	}

//    @Test
//    void treeCopyOverExistingTree_RecursiveOnOverwriteOnSmashOff_OverwritesFiles() {
//        def vfs=VFS.manager
//        def from=vfs.toFileObject(testFsReadOnlyRoot)
//        def to=vfs.toFileObject(testFsWriteRoot)
//        def targetDir="${testFsWriteRoot}"
//
//        assertTrue "Target dir '${to}' must exist prior to testing copy function",to.exists()
//
//        new File("${testFsWriteRoot}/test-files/test-subdir").mkdirs()
//        expectedFiles.each {
//            new File("${testFsWriteRoot}/test-files/${it}").text = '1234'
//        }
//        CopyMoveOperations.copy(from,to,false,true,true)
//
//        expectedFiles.each {
//            def file = new File("${testFsWriteRoot}/test-files/${it}")
//            assertTrue "Expected ${file}",file.exists()
//            assertEquals "Expected content to have changed for ${file}", new File("${testFsReadOnlyRoot}/${it}").text, file.text
//        }
//    }
//
//	@Test(expected=FileActionException)
//	void copyDirectoryToExistingDirectoryWithSameNamedSubfolderAndSomeSameNameFiles_RecursiveOnOverwriteOffSmashOff_PopulatesSubfolderButFailsOnFirstSameNamedChild() {
//		def vfs=VFS.manager
//		def from=vfs.toFileObject(new File("${testFsReadOnlyRoot}/test-subdir"))
//		def to=vfs.toFileObject(testFsWriteRoot)
//		def targetDir="${testFsWriteRoot}/test-subdir"
//		new File(targetDir).mkdirs()
//
//		assertTrue "Target dir '${to}' must exist prior to testing copy function",to.exists()
//
//		// Copy first time should pass
//		CopyMoveOperations.copy(from,to,false,false,true)
//		def expected=to.resolveFile("test-subdir/file3.txt")
//		assertTrue "Expected ${targetDir}/file3.txt",expected.exists()
//
//		// Second copy should fail, because destination files will now exist
//		CopyMoveOperations.copy(from,to,false,false,true)
//	}
//
//	@Test
//	void copyDirectoryToExistingDirectory_RecursiveOnOverwriteOnSmashOff_AddsSubfolder() {
//		def vfs=VFS.manager
//		def from=vfs.toFileObject(testFsReadOnlyRoot)
//		def to=vfs.toFileObject(testFsWriteRoot)
//		def targetDir="${testFsWriteRoot}/${from.name.baseName}"
//
//		println "Copying from folder '${from}' to folder '${to}'"
//		println "Final result should be '${to}/${from.name.baseName}"
//		assertFalse "Target directory must not exist at this point",new File(targetDir).exists()
//
//		CopyMoveOperations.copy(from,to,false,true,true)
//		assertTrue "Target directory should exist",new File(targetDir).exists()
//
//		expectedFiles.each {
//			def expected=to.resolveFile(from.name.baseName).resolveFile(it)
//			assertTrue "Expected '${targetDir}/${it}'", expected.exists()
//			assertTrue "Expected '${targetDir}/${it}'", new File("${targetDir}/${it}").exists()
//			assertEquals FileType.FILE,expected.type
//		}
//
//	}
//
//	@Test
//	void copyDirectoryToExistingDirectorySameNamedSubfolder_RecursiveOnOverwriteOnSmashOff_PopulatesSubfolderOverwritingExistingFiles() {
//		def vfs=VFS.manager
//		def srcDir=new File("${testFsReadOnlyRoot}/test-subdir")
//		def from=vfs.toFileObject(srcDir)
//		def to=vfs.toFileObject(testFsWriteRoot)
//		def targetDir="${testFsWriteRoot}/test-subdir"
//		new File(targetDir).mkdirs()
//
//		assertTrue "Target dir '${to}' must exist prior to testing copy function",to.exists()
//
//		CopyMoveOperations.copy(from,to,false,true,true)
//		def expected=to.resolveFile("test-subdir/file3.txt")
//		assertTrue "Expected ${targetDir}/file3.txt",expected.exists()
//
//		(3..4).each {
//			def f=new File("${targetDir}/file${it}.txt")
//			assertTrue new File("${targetDir}/file${it}.txt").text == new File("${srcDir}/file${it}.txt").text
//			f.text = "${it}"
//			assertFalse f.text == new File("${srcDir}/file${it}.txt").text
//		}
//
//		CopyMoveOperations.copy(from,to,false,true,true)
//		(3..4).each {
//			assertTrue new File("${targetDir}/file${it}.txt").text == new File("${srcDir}/file${it}.txt").text
//		}
//	}
//
//	@Test(expected=FileActionException)
//	void copyDirectoryToExistingDirectoryWithSameNamedSubfolder_RecursiveOverwriteOnSmashOff_PopulatesSubfolderOverwritingFilesButFailsIfSourceIsFileAndDestinationIsFolder() {
//		def vfs=VFS.manager
//		def srcDir=new File("${testFsReadOnlyRoot}/test-subdir")
//		def from=vfs.toFileObject(srcDir)
//		def to=vfs.toFileObject(testFsWriteRoot)
//		def targetDir="${testFsWriteRoot}/test-subdir"
//		new File("${targetDir}/file4.txt").mkdirs()
//
//		CopyMoveOperations.copy(from,to,false,true,true)
//	}
//
//
//	// ------------------------------------------------------------------------
//	// Directory -> File, No Smash, No Filter
//	// ------------------------------------------------------------------------
//	@Test(expected=FileActionException)
//	void copyDirectoryToExistingFile_SmashOffOverwriteOnRecursiveOn_WillFail() {
//		def vfs=VFS.manager
//		def srcDir=new File("${testFsReadOnlyRoot}/test-subdir")
//		def targetDir="${testFsWriteRoot}/test-subdir"
//		new File(targetDir).text="FOO"
//		def from=vfs.toFileObject(srcDir)
//		def to=vfs.toFileObject(testFsWriteRoot)
//
//		CopyMoveOperations.copy(from,to,false,true,true)
//	}
//
//	// ------------------------------------------------------------------------
//	// Directory -> Directory/File, With Smash, No Filter
//	// ------------------------------------------------------------------------
//	@Test
//	void copyDirectoryToExistingFile_SmashOn_WithReplaceFileWithFolder() {
//		def vfs=VFS.manager
//		def srcDir=new File("${testFsReadOnlyRoot}/test-subdir")
//		def target="${testFsWriteRoot}/test-subdir"
//		new File(target).text="FOO"
//		def from=vfs.toFileObject(srcDir)
//		def to=vfs.toFileObject(new File(target))
//
//		assertEquals "Expects target to be a file before performing copy",FileType.FILE,to.type
//
//		CopyMoveOperations.copy(from,to,true,false,false)
//
//		assertEquals "Expects target to be a folder after performing copy",FileType.FOLDER,to.type
//	}
//
//	@Test
//	void copyDirectoryToExistingDirectory_SmashOn_WillDeleteTargetFolderAndCopySourceFolder() {
//		def vfs=VFS.manager
//		def srcDir=new File("${testFsReadOnlyRoot}")
//		def targetDir=new File("${testFsWriteRoot}/do_not_smash_me")
//		def from=vfs.toFileObject(srcDir)
//		def to=vfs.toFileObject(targetDir)
//
//		println """Smash copy from '${CopyMoveOperations.friendlyURI(from)}'
//                   to '${CopyMoveOperations.friendlyURI(to)}' should result
//                   in '${to.parent.resolveFile(from.name.baseName)}'"""
//
//		targetDir.mkdirs()
//		assertTrue "Before copying, ensure that the target folder exists",to.exists()
//
//		CopyMoveOperations.copy(from,to,true,false,false)
//		to=vfs.toFileObject(new File("${testFsWriteRoot}/${from.name.baseName}"))
//		assertTrue "After copying, the target folder should have the same base name as the source folder", to.exists()
//		assertEquals FileType.FOLDER,to.type
//
//		from.children.each {
//			def expected=to.resolveFile(it.name.baseName)
//			assertTrue "Expected to see ${CopyMoveOperations.friendlyURI(expected)} in target folder",expected.exists()
//		}
//	}
//
//	// ------------------------------------------------------------------------
//	// File -> Directory, With Filter
//	// ------------------------------------------------------------------------
//	@Test
//	void copyFileWithPatternFilterToExistingDirectory_AddsToDirectoryIfNonExistingFileNameSuppliedAndFilterMatches() {
//		def vfs=VFS.manager
//		def from=vfs.toFileObject(new File("${testFsReadOnlyRoot}/file2.txt"))
//		def to=vfs.toFileObject(testFsWriteRoot)
//
//		CopyMoveOperations.copy(from,to,false,false,false,/file1\.txt/)
//		assertFalse new File("${testFsWriteRoot}/file2.txt").exists()
//		assertFalse new File("${testFsWriteRoot}/file1.txt").exists()
//
//		CopyMoveOperations.copy(from,to,false,false,false,~/file2\.txt/)
//		assertTrue new File("${testFsWriteRoot}/file2.txt").exists()
//		assertFalse new File("${testFsWriteRoot}/file1.txt").exists()
//	}
//
//	@Test(expected=FileActionException)
//	void copyFileWithPatternToExistingDirectory_OverwriteOff_FailsIfTargetFileExistsAndFileMatches() {
//		def vfs=VFS.manager
//		def from=vfs.toFileObject(new File("${testFsReadOnlyRoot}/file2.txt"))
//		def to=vfs.toFileObject(testFsWriteRoot)
//
//		CopyMoveOperations.copy(from,to,false,false,false)
//		assertTrue "The target file must exist, before running the filter test",
//			new File("${testFsWriteRoot}/file2.txt").exists()
//
//		CopyMoveOperations.copy(from,to,false,false,false,/file1\.txt/)
//		assertFalse new File("${testFsWriteRoot}/file2.txt").exists()
//
//		CopyMoveOperations.copy(from,to,false,false,false,~/file2\.txt/)
//	}
//
//	@Test
//	void copyFileWithPatternToExistingDirectory_OverwriteOn_ReplacesIfTargetFileExists() {
//		def vfs=VFS.manager
//		def srcFile=new File("${testFsReadOnlyRoot}/file2.txt")
//		def from=vfs.toFileObject(srcFile)
//		def to=vfs.toFileObject(testFsWriteRoot)
//
//		CopyMoveOperations.copy(from,to,false,false,false)
//		def fileUnderTest=new File("${testFsWriteRoot}/file2.txt")
//		assertTrue "The target file must exist, before running the filter test",
//			fileUnderTest.exists()
//		fileUnderTest.text="FOOBAR"
//		assertNotEquals srcFile.text,fileUnderTest.text
//
//		CopyMoveOperations.copy(from,to,false,true,false,/file1\.txt/)
//		assertNotEquals "If the filter does nto match, then file must not be overwritten",
//			srcFile.text,fileUnderTest.text
//
//		CopyMoveOperations.copy(from,to,false,true,false,~/file2\.txt/)
//		assertEquals srcFile.text,fileUnderTest.text
//    }
//
//	// TODO: ISSUE #4 - What about using a closure as a filter?
//
//    @Test
//    void copyFileWithExcludeSelfSelectorToExistingDirectory_CopiesFilesBelowDirectory() {
//        def vfs=VFS.manager
//        def srcFile=new File("${testFsReadOnlyRoot}")
//        def from=vfs.toFileObject(srcFile)
//        def to=vfs.toFileObject(testFsWriteRoot)
//
//        CopyMoveOperations.copy(from,to,false,false,true,Selectors.EXCLUDE_SELF)
//
//        assertTrue new File(testFsWriteRoot,'file1.txt').exists()
//        assertTrue new File(testFsWriteRoot,'file2.txt').exists()
//        assertTrue new File(testFsWriteRoot,'test-subdir').exists()
//        assertTrue new File(testFsWriteRoot,'test-subdir/file3.txt').exists()
//
//    }
//
//	// ------------------------------------------------------------------------
//	// Directory -> Directory, With Filter
//	// ------------------------------------------------------------------------
//
	// ------------------------------------------------------------------------
	// Miscellaneous tests
	// ------------------------------------------------------------------------
	void 'Copy non-existing source raised exception'() {
        given:
        final VfsURI from = readRootURI.resolve('non_existing_file')
        final VfsURI to = writeRootURI

        when:
		CopyMoveOperations.copy(from,to,false,false,false)

        then:
        thrown(FileActionException)
	}

    static boolean exists(final File filename) {
        filename.exists()
    }

    static boolean exists(final String filename) {
        new File(filename).exists()
    }

    static boolean exists(final File root, final File relative) {
        new File(root,relative).exists()
    }
}
