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
// ============================================================================
// Copyright (C) Schalk W. Cronje 2012
//
// This software is licensed under the Apche License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
// ============================================================================

package org.ysb33r.groovy.dsl.vfs.impl

import static org.junit.Assert.*
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.Ignore
import org.apache.commons.vfs2.FileSystemOptions
import org.apache.commons.vfs2.VFS
import org.ysb33r.groovy.dsl.vfs.URI

public class UtilTest {

	static def testFsReadOnlyRoot = new File("${System.getProperty('TESTFSREADROOT')}/src/test/resources/test-files")
	static def testFsURI

	@BeforeClass
	static void createFileStructure()  {
		assert testFsReadOnlyRoot.exists()
		
		testFsURI = new URI(testFsReadOnlyRoot).toString()
			
	}

	@Test
	void settingValidIntegerValue() {
		def vfs=VFS.getManager()
		def ctrl=new FileSystemOptions()
		def ftp = vfs.getFileSystemConfigBuilder("ftp")
		def oldValue = ftp.getSoTimeout(ctrl)
		def newValue = 10001
		
		assertTrue oldValue != newValue
		
		def fsOpts = Util.buildOptions( 'vfs.ftp.soTimeout': newValue, vfs )
		assertEquals newValue, ftp.getSoTimeout(fsOpts)
		
		newValue = 10002
		fsOpts = Util.buildOptions( 'vfs.ftp.soTimeout': "${newValue}", vfs )
		assertEquals newValue, ftp.getSoTimeout(fsOpts)
	}

	@Test
	void settingValidBooleanValue() {
		def vfs=VFS.getManager()
		def ctrl=new FileSystemOptions()
		def ftp = vfs.getFileSystemConfigBuilder("ftp")
		def oldValue = ftp.getPassiveMode(ctrl)
		def newValue = !oldValue
		
		def fsOpts = Util.buildOptions( 'vfs.ftp.passiveMode': newValue, vfs )
		assertEquals "Setting a new native boolean value (${newValue})", newValue, ftp.getPassiveMode(fsOpts)
		
		// true/false test #1
		newValue = !newValue	
		fsOpts = Util.buildOptions( 'vfs.ftp.passiveMode': "${newValue ? 'true' : 'false'}", vfs )
		assertEquals "Setting from GString true/false #1 (${newValue})", newValue, ftp.getPassiveMode(fsOpts)

		// true/false test #2
		newValue = !newValue		
		fsOpts = Util.buildOptions( 'vfs.ftp.passiveMode': "${newValue ? 'true' : 'false'}", vfs )
		assertEquals "Setting from GString true/false #2 (${newValue})", newValue, ftp.getPassiveMode(fsOpts)

		// y/n test #1
		newValue = !newValue		
		fsOpts = Util.buildOptions( 'vfs.ftp.passiveMode': "${newValue ? 'y' : 'n'}", vfs )
		assertEquals "Setting from GString y/n #1 (${newValue})", newValue, ftp.getPassiveMode(fsOpts)

		// y/n test #2
		newValue = !newValue		
		fsOpts = Util.buildOptions( 'vfs.ftp.passiveMode': "${newValue ? 'y' : 'n'}", vfs )
		assertEquals "Setting from GString y/n #2 (${newValue})", newValue, ftp.getPassiveMode(fsOpts)

		// 1/0 test #1
		newValue = !newValue
		String n = newValue ? '1' : '0'
		fsOpts = Util.buildOptions( 'vfs.ftp.passiveMode': n , vfs )
		assertEquals "Setting from GString 1/0 #1 (${newValue})", newValue, ftp.getPassiveMode(fsOpts)

		// 1/0 test #2
		newValue = !newValue
		n = newValue ? '1' : '0'
		fsOpts = Util.buildOptions( 'vfs.ftp.passiveMode': n, vfs )
		assertEquals "Setting from GString 1/0 #2 (${newValue})", newValue, ftp.getPassiveMode(fsOpts)

		Boolean b = !newValue
		fsOpts = Util.buildOptions( 'vfs.ftp.passiveMode': b, vfs )
		assertEquals "Setting from native Boolean (${newValue})", !newValue, ftp.getPassiveMode(fsOpts)
	}		

	@Ignore
	@Test(expected=org.ysb33r.groovy.dsl.vfs.OptionException)
	void settingInvalidBooleanValue() {
		def vfs=VFS.getManager()
		def ctrl=new FileSystemOptions()
		def ftp = vfs.getFileSystemConfigBuilder("ftp")
		def oldValue = ftp.getPassiveMode(ctrl)
		
		fail "Need to provide a class which cannot convert to Boolean"
		Util.buildOptions( 'vfs.ftp.passiveMode': 0 , vfs )
	}

	@Test
	void overridingFileSystemOptions() {
		def vfs=VFS.getManager()
		def ctrl=new FileSystemOptions()
		def ftp = vfs.getFileSystemConfigBuilder("ftp")
		ftp.setSoTimeout(ctrl,9999)
		def newValue = 10002
		
		def fsOpts = Util.buildOptions( 'vfs.ftp.soTimeout': newValue, vfs, ctrl )
		assertTrue fsOpts != ctrl
	}
	
	@Test(expected=org.apache.commons.vfs2.FileSystemException)
	void invalidSchemeShouldRaiseException() {
		Util.buildOptions( 'vfs.foobar.property': 10000, VFS.getManager() )
	}
	
	@Test
	void resolvingURIWithNoExtraOptionsShouldFindIt() {
		
		def vfs=VFS.getManager()
		def fso=new FileSystemOptions()
		def r= Util.resolveURI(vfs,fso,"${testFsURI}/file1.txt")
		
		assertTrue "file.txt is a test file and should be found at ${testFsURI}/file1.txt", r.exists() 
	} 
	
    @Test
    void resolvingURIwithExtraOptionsShouldSetCorrectFilesystemOptions() {
        def vfs=VFS.getManager()
        def originalOptions=new FileSystemOptions()
        def ftp = vfs.getFileSystemConfigBuilder("ftp")
        ftp.setSoTimeout(originalOptions,9999)
        ftp.setUserDirIsRoot(originalOptions,true)
        
        def newValue = 10002
        
        def u = new URI ("ftp://some.host:1234?vfs.ftp.soTimeout=${newValue}")
        def newOptions = Util.buildOptions(u,vfs,originalOptions)
        assertEquals newValue,ftp.getSoTimeout(newOptions)
        assertEquals true,ftp.getUserDirIsRoot(newOptions)
    }

}


