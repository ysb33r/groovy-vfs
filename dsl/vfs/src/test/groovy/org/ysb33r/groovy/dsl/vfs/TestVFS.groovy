// ============================================================================
// Copyright (C) Schalk W. Cronje 2012
//
// This software is licensed under the Apche License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
// ============================================================================

package org.ysb33r.groovy.dsl.vfs

import static org.junit.Assert.*

import org.apache.commons.vfs2.FileSystemOptions
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.Ignore

 class TestVFS {

	static def supportedSchemes = [ 
		'zip','res','gz','war','par','ear','ram',
		'file','jar','sar','ejb3','tmp','tar',
		'tbz2','tgz','bz2',
		'ftp','ftps',
		'http','https',
		'sftp',
		// 'webdav'
		// 'smb','cifs'
		// 'mime'
	]
	
	static def testFsReadOnlyRoot = new File('src/test/resources/test-files')
	static def testFsURI
	static def testFsWriteRoot
	static def testFsWriteURI
	
	static def expectedFiles= ['file1.txt','file2.txt']
	
	@BeforeClass
	static void createFileStructure()  {
		assert testFsReadOnlyRoot.exists()
		
		testFsURI = new URI(testFsReadOnlyRoot).toString()
			
		testFsWriteRoot= new File( "${System.getProperty('TESTFSWRITEROOT') ?: 'build/tmp/test-files'}/file")
		testFsWriteURI= new URI(testFsWriteRoot).toString()
	}

	@AfterClass
	static void cleanFileStructure() {
		
	}

	@Before
	void setUp() {
		if (!testFsWriteRoot.exists()) {
			testFsWriteRoot.mkdirs()
		}
	}

	@After
	void tearDown() {
		testFsWriteRoot.delete()
	}

	@Test
	void defaultCtorIsLikeVFSDefaultButNoLogger() {
		def vfs = new VFS()
		assertNotNull vfs.fsMgr
		assertNotNull vfs.defaultFSOptions
		
		def schemes = vfs.fsMgr.schemes.sort()
		supportedSchemes.each { 
			assertTrue "Expected to support '${it}', Only had ${schemes.toString()}", schemes.contains(it) 
		} 	
	}

	@Test
	void basicFileListingShouldProvideAccessToFileNames() {
		def vfs = new VFS()		
		def uri= testFsURI

		def files= vfs.ls uri		
		files.each {   
			assertTrue "${it} is not an expected file in ${testFsURI}",expectedFiles.contains(it.name.baseName) 
		}				
		
		vfs.ls (uri) {
			assertTrue "${it} is not an expected file in ${testFsURI}",expectedFiles.contains(it.name.baseName)			
		}
		
	}
	
	@Test 
	void fileListingWithRegexFilterShouldOnlyReturnMatchedFileNames() {
		def vfs = new VFS()

		def files= vfs.ls testFsURI,filter:~/file2\.txt/
		
		assertEquals 1,files.size()
		assertEquals "file2.txt",files[0].name.baseName 
	}

	@Test
	void basicFileReadingShouldProvideSameAsIfUsingGroovyFile() {
		def vfs = new VFS()
		def uri= "${testFsURI}/${expectedFiles[0]}"
		def file= new File("${testFsReadOnlyRoot}/${expectedFiles[0]}")
		
		def contentFromCode = vfs.cat uri
		assertEquals file.text,contentFromCode.text

		contentFromCode=null		
		vfs.cat(uri) {
			contentFromCode=it.text
		}
		assertEquals file.text,contentFromCode
		
	}
	
	@Test
	void creatingDirectoryShouldCreateParentsToo() {
		def vfs = new VFS()
		
		assertTrue !(new File("${testFsWriteRoot}/one/two/three").exists()) 
		vfs.mkdir("${testFsWriteURI}/one/two/three")
		assertTrue new File("${testFsWriteRoot}/one/two/three").exists() 
	}
	
	@Test
	void copyFileToExistingDirectoryAddsToDirectory() {
		
	} 
	
	// void copyFileOverExistingDirectoryWithOverwriteWithoutSmashFails() {
	// void copyFileOverExistingDirectoryWithOverwriteWithSmashReplacesDirectoryWithFile() {
	// void copyDirectoryToDirectoryAddsToDirectory()
	// void copyDirectoryOverExistingDirectoryWithoutOverwriteFails()
	// void copyDirectoryOverExistingDirectoryWithOverwriteReplacesDirectory() 
	// void copyDirectoryWIthFilterSelectivelyCopiesFilesToNewDestination()
	
	@Test
	void scriptAndLeftShiftShouldExecuteMultipleStatement() {

		def vfs = new VFS()
		def uri= testFsURI
		def file= new File("${testFsReadOnlyRoot}/${expectedFiles[0]}")
		
		vfs << {
			ls (uri) {
				assertTrue "${it} is not an expected file in ${testFsURI}",expectedFiles.contains(it.name.baseName)				
			}
			
			cat (file) {
				assertEquals file.text,it.text
			}
		}
	
		vfs << {
			ls (uri,filter:~/file1\.txt/) {
				cat (it) { 
					assertEquals file.text,it.text
				}
			}
			mkdir "${testFsWriteURI}/one/two/three"
			assertEquals 1,ls ("${testFsWriteURI}/one/two", filter:~/three/) .size()
		}
/*		
		vfs << {
			ls (uri,filter:~/file1\.txt/) cat(it) { 
					assertEquals file.text,it.text
			}
		}
*/		
	}


}
