/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2015
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
package org.ysb33r.groovy.dsl.vfs

import static org.junit.Assert.*

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;	

import org.ysb33r.groovy.dsl.vfs.URI;

import groovyx.net.http.URIBuilder

class TestURI {

	@BeforeClass
	static void setUpBeforeClass()  {
	}

	@AfterClass
	static void tearDownAfterClass()  {
	}

	@Before
	void setUp() {
	}

	@After
	void tearDown() {
	}

/*	
	@Test
	void test() {
		
		 // Returns a closure which can only take a closure as a parameter
		 def vfs = VFS.newFS ( ..... )
		 vfs {
		 	copy from:URI, to: URI // must resolve copy in scope of VFS
		 	move from:URI, to: URI // must resolve move in scope of VFS
		 	
		 	URI.each
		 }
		 
		fail("Not yet implemented");
	}
*/

	@Test(expected=java.net.URISyntaxException)
	public void noSchemeShouldFail() {
		def u = new URI("abvd")
	}

	@Test(expected=org.ysb33r.groovy.dsl.vfs.URIException)
	public void emptyShouldFail() {
		def u = new URI("")
	}

	@Test
	public void basicURIShouldPass() {
		def u = new URI('ftp://127.0.0.1/path')
		
		assertEquals 'ftp://127.0.0.1/path',u.toString()
		assertEquals 0,u.properties.size()
	}

	@Test
	public void queryStringContainingVFSItemsWillGetDropped() {
		def u = new URI('ftp://127.0.0.1/path?vfs.ftp.passiveMode=true')
	
		assertEquals 'ftp://127.0.0.1/path',u.toString()
		
		def p= u.properties

		assertEquals 1,p.size()
		assertEquals 1,p.ftp.size()
		assertEquals 'true',p.ftp.passiveMode

		u = new URI('ftp://127.0.0.1/path?vfs.ftp.passiveMode=true&vfs.ftp.soTimeout=10')
		
		p= u.properties
		assertEquals 1,p.size()
		assertEquals 2,p.ftp.size()
		assertEquals 'true',p.ftp.passiveMode
		assertEquals '10',p.ftp.soTimeout

		u = new URI('ftp://127.0.0.1/path?vfs.ftp.passiveMode=true&vfs.ftp.soTimeout=10&vfs.sftp.userDirIsRoot=false')
		
		p= u.properties
		assertEquals 2,p.size()
		assertEquals 2,p.ftp.size()
		assertEquals 'true',p.ftp.passiveMode
		assertEquals '10',p.ftp.soTimeout
		assertEquals 1,p.sftp.size()
		assertEquals 'false',p.sftp.userDirIsRoot

	}

	@Test
	public void queryStringWithMixedItemsWillDropVFSItems() {
		def uriText = 'ftp://127.0.0.1/path?vfs.ftp.passiveMode=true&large=5&small=3'
		def u = new URI(uriText)
		
		assertEquals new URIBuilder(u.toString()).query,new URIBuilder('ftp://127.0.0.1/path?large=5&small=3').query
		
		def p= u.properties
		assertEquals 1,p.size()
		assertEquals 1,p.ftp.size()
		assertEquals 'true',p.ftp.passiveMode
	}
	
	@Test
	public void userInfoShouldRemain() {
		def u = new URI('ftp://ysb33r:Password@127.0.0.1/path')
		
		assertEquals 'ftp://ysb33r:Password@127.0.0.1/path',u.toString()
		assertEquals 0,u.properties.size()
	}

	@Test
	public void obfuscatedUserInfoShouldBeAccepted() {
		def u = new URI('ftp://ysb33r:{D7B82198B272F5C93790FEB38A73C7B8}@127.0.0.1/path')
		
		assertEquals 'ftp://ysb33r:{D7B82198B272F5C93790FEB38A73C7B8}@127.0.0.1/path',u.toString()
		assertEquals 0,u.properties.size()

		u = new URI('ftp://ysb33r:{D7B82198B272F5C93790FEB38A73C7B8}@127.0.0.1/path?abc=dec&vfs.ftp.passive=true#fragment')
		
		assertEquals 'ftp://ysb33r:{D7B82198B272F5C93790FEB38A73C7B8}@127.0.0.1/path?abc=dec#fragment',u.toString()
		assertEquals 1,u.properties.size()
	}

	@Test
	public void basicMultiComponentURIShouldRemain() {
		def u = new URI('tar:gz:http://anyhost/dir/mytar.tar.gz!/mytar.tar!/path/in/tar/README.txt')
		assertEquals 'tar:gz:http://anyhost/dir/mytar.tar.gz!/mytar.tar!/path/in/tar/README.txt',u.toString()
		assertEquals 0,u.properties.size()
	}

	@Test
	public void usingGroovyFileObjectShouldYieldFileURI() {
		def f= new File('.')
		def u= new URI(f)
		assertEquals 0,u.properties.size()
		assertEquals f.toURI().normalize().toString().replaceFirst('file:','file://'),u.toString()
	}

	@Test
	public void usingGroovyFileObjectWithSpaceShouldPass() {
		def f= new File('filename with spaces')
		def u= new URI(f)
		assertEquals 0,u.properties.size()
		assertEquals f.toURI().normalize().toString().replaceFirst('file:','file://'),u.toString()
	}


}
