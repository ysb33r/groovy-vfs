// ============================================================================
// Copyright (C) Schalk W. Cronje 2012 - 2014
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
// ============================================================================

package org.ysb33r.groovy.dsl.vfs

import org.apache.commons.logging.LogFactory

import static org.junit.Assert.*

import org.apache.commons.vfs2.FileSystemOptions
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder
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
	
	static File testFsReadOnlyRoot = new File("${System.getProperty('TESTFSREADROOT')}/src/test/resources/test-files")
	static String testFsURI
	static File testFsWriteRoot
	static String testFsWriteURI
	
	static def expectedFiles= ['file1.txt','file2.txt', 'test-subdir/file3.txt','test-subdir/file4.txt']
	
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
		testFsWriteRoot.mkdirs()
	}

	@After
	void tearDown() {
		testFsWriteRoot.deleteDir()
	}

	@Test
	void defaultCtorIsLikeVFSDefaultButNoLogger() {
		def vfs = new VFS()
		assertNotNull vfs.fsMgr
		assertNotNull vfs.defaultFSOptions
		assertTrue vfs.fsMgr.metaClass.respondsTo(vfs.fsMgr,"loggerInstance").size() > 0
        
		def schemes = vfs.fsMgr.schemes.sort()
		supportedSchemes.each { 
			assertTrue "Expected to support '${it}', Only had ${schemes.toString()}", schemes.contains(it) 
		} 	
	}

	@Test
	void ctorWithFileStoreAndLoggerShouldNotThrow() {
		def vfs = new VFS (
			logger : LogFactory.getLog('testvfs'),
				temporaryFileStore : "${testFsWriteRoot}/vfs".toString()
		)
		assertNotNull vfs.fsMgr
	}

	@Test
	void basicFileListingShouldProvideAccessToFileNames() {
		def vfs = new VFS()		
		def uri= testFsURI

		def files= vfs.ls uri		
		files.each {   
			assertTrue "${it} is not an expected file in ${testFsURI}",expectedFiles.contains(it.name.baseName) || it.type == FileType.FOLDER
		}				
		
		vfs.ls (uri) {
			assertTrue "${it} is not an expected file in ${testFsURI}",expectedFiles.contains(it.name.baseName) || it.type == FileType.FOLDER			
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

        vfs.cat (uri) { contentFromCode ->
    		assertEquals file.text,contentFromCode.text
        }
	}
	
	@Test
	void creatingDirectoryShouldCreateParentsToo() {
		def vfs = new VFS()
		
		def testDir=new File("${testFsWriteRoot}/one/two/three")
		assertFalse testDir.exists() 
		vfs.mkdir("${testFsWriteURI}/one/two/three")
		assertTrue testDir.exists() 
	}

     @Test(expected=FileActionException)
     void creatingDirectoryShouldCreateParentsTooUnlessIntermediatesFalse() {
         def vfs = new VFS()

         def testDir=new File("${testFsWriteRoot}/one/two/three")
         assertFalse testDir.exists()
         vfs.mkdir("${testFsWriteURI}/one/two/three", intermediates:false)
     }


    @Test
	void copyFileToExistingDirectoryAddsToDirectoryIfRecursive() {
		def vfs = new VFS()
		vfs.cp( recursive:true, testFsURI, testFsWriteURI ) 
		
		expectedFiles.each {
			def f=new File("${testFsWriteRoot}/test-files/${it}")
			assertTrue "Expected to find ${f} on disk", f.exists()
		} 
	} 
	
    @Test
    void settingOptionsShouldUpdateDefaultOptions() {
        def vfs = new VFS('vfs.ftp.passiveMode' : true)
        def fscb = vfs.fsMgr.getFileSystemConfigBuilder('ftp') as FtpFileSystemConfigBuilder
        
        assertTrue fscb.getPassiveMode( vfs.defaultFSOptions )
        vfs.options 'vfs.ftp.passiveMode' : false 
        assertFalse fscb.getPassiveMode( vfs.defaultFSOptions )
   }
    
    @Test
    void settingOptionsFromClosureShouldUpdateDefaultOptions() {
        def gfs = new VFS('vfs.ftp.passiveMode' : false)
        def fscb = gfs.fsMgr.getFileSystemConfigBuilder('ftp') as FtpFileSystemConfigBuilder
        
        assertFalse fscb.getPassiveMode( gfs.defaultFSOptions )
        gfs.options {
            ftp {
              passiveMode  true
            } 
        }
        assertTrue fscb.getPassiveMode( gfs.defaultFSOptions ) 
    }
    
	@Test
	void scriptAndLeftShiftShouldExecuteMultipleStatement() {

		def vfs = new VFS()
		def uri= testFsURI
		def file= new File("${testFsReadOnlyRoot}/${expectedFiles[0]}")
		
		vfs  {
			ls (uri) {
				if(it.name.baseName != 'test-subdir') {
					assertTrue "${it} is not an expected file in ${testFsURI}",expectedFiles.contains(it.name.baseName)
				}				
			}
			
			cat (file) {
				assertEquals file.text,it.text
			}
		}
	
		vfs  {
			ls (uri,filter:~/file1\.txt/) {
				cat (it) { 
					assertEquals file.text,it.text
				}
			}
			mkdir "${testFsWriteURI}/one/two/three"
			assertEquals 1,ls ("${testFsWriteURI}/one/two", filter:~/three/) .size()
			
			cp testFsURI,"${testFsWriteURI}/one/two/three", recursive:true
			assertEquals 2,ls ("${testFsWriteURI}/one/two/three/test-files", filter:~/file\d\.txt/) .size()
			
			mv "${testFsWriteURI}/one/two/three","${testFsWriteURI}/one/two/four"
			assertEquals 2,ls ("${testFsWriteURI}/one/two/four/test-files", filter:~/file\d\.txt/) .size()

		}
	}

    @Test
    void CreatingVFS_ignoreDefaultProviders_shouldNotLoadAnyPlugins() {
        def vfs= new VFS(ignoreDefaultProviders:true)

        assertFalse vfs.fsMgr.hasProvider('file')
    }

    @Test
    void TypeShouldReturnFileFolderOrNonExistent() {
        def vfs = new VFS()
        def uri= testFsURI
        def file= new File("${testFsReadOnlyRoot}/${expectedFiles[0]}")


        assertTrue vfs.isFolder(testFsURI)
        assertTrue vfs.isFile(file)
        assertFalse vfs.exists( new URI(new File(testFsReadOnlyRoot,'non-existent-file') ) )

    }

     @Test(expected=FileSystemException)
     void LastModifiedTimeOfURIShouldThrowExceptionIfURINotExisting() {
         def vfs = new VFS()
         assertTrue vfs.mtime(testFsURI) > 0

         def file= new File("${testFsReadOnlyRoot}/somewhere/a/non-existing-file")
         assertEquals 0,vfs.mtime(file)

     }

	 @Test
	 void EchoFromStringMustUpdateFile() {
		 def vfs = new VFS()

		 File dest = new File(testFsWriteRoot,'echo-file.txt')

		 vfs {
			 overwrite dest with 'test text'
		 }

		 assertEquals 'test text',dest.text

		 vfs {
			 append dest with ' more text'
		 }

		 assertEquals 'test text more text',dest.text
	 }

	 @Test
	 void EchoFromClosureMustUpdateFile() {
		 def vfs = new VFS()

		 File dest = new File(testFsWriteRoot,'echo-file.txt')

		 vfs {
			 overwrite dest, { it << 'test text' }
		 }

		 assertEquals 'test text',dest.text

		 vfs {
			 append dest, { it << ' more text' }
		 }

		 assertEquals 'test text more text',dest.text
	 }
 }
