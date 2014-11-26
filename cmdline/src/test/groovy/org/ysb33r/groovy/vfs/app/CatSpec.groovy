// ============================================================================
// (C) Copyright Schalk W. Cronje 2014
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================
package org.ysb33r.groovy.vfs.app

import spock.lang.*
import org.ysb33r.groovy.dsl.vfs.URI as vfsURI
import org.ysb33r.groovy.dsl.vfs.VFS

class CatSpec extends Specification {

    static final File READROOT= new File("${System.getProperty('TESTFSREADROOT')}/test-files")
    static final File EXPECTEDROOT= new File("${System.getProperty('TESTFSREADROOT')}/test-files/cat-expected")

    def vfs = new VFS()
    OutputStream capture = new ByteArrayOutputStream()

    void setup() {
    }

    void cleanup() {
        capture.close()
    }

    def "Cat a local file without options" () {
        given:
            File testfile= new File(READROOT,'file1.txt')
            List<vfsURI> uris = [ new vfsURI( testfile ) ]
            Cmd cmd = new Cat( 'uris' : uris )
            cmd.out = new PrintStream(capture)

        when:
            cmd.run(vfs)

        then:
            capture.toString() == testfile.text
    }

    def "Cat a local file and number all lines" () {
        given:
            File testfile= new File(READROOT,'file1.txt')
            List<vfsURI> uris = [ new vfsURI( testfile ) ]
            Cmd cmd = new Cat( 'uris' : uris, numberLines : true )
            cmd.out = new PrintStream(capture)

        when:
            cmd.run(vfs)

        then:
            capture.toString() == new File(EXPECTEDROOT,'file1-numbered.txt').text
    }

    def "Cat two local files and number all lines, restarting with every file" () {
        given:
        File testfile= new File(READROOT,'file1.txt')
        List<vfsURI> uris = [ new vfsURI( testfile ), new vfsURI(new File(READROOT,'file2.txt')) ]
        Cmd cmd = new Cat( 'uris' : uris, numberLines : true )
        cmd.out = new PrintStream(capture)

        when:
        cmd.run(vfs)

        then:
        capture.toString() == new File(EXPECTEDROOT,'file1-file2-numbered.txt').text
    }

    def "Cat a local file and number non-blank lines (with last line not blank)" () {
        given:
            File testfile= new File(READROOT,'file2.txt')
            List<vfsURI> uris = [ new vfsURI( testfile ) ]
            Cmd cmd = new Cat( 'uris' : uris, numberLines : true, numberNonEmptyLines : true )
            cmd.out = new PrintStream(capture)

        when:
            cmd.run(vfs)

        then:
            capture.toString() == new File(EXPECTEDROOT,'file2-numbered-notblanks.txt').text
    }

    def "Cat a local file and number non-blank lines (with last line blank)" () {
        given:
        File testfile= new File(READROOT,'file4.txt')
        List<vfsURI> uris = [ new vfsURI( testfile ) ]
        Cmd cmd = new Cat( 'uris' : uris, numberLines : true, numberNonEmptyLines : true )
        cmd.out = new PrintStream(capture)

        when:
            cmd.run(vfs)

        then:
            capture.toString() == new File(EXPECTEDROOT,'file4-numbered-notblanks.txt').text
    }


    def "Suppress repeating blank lines in a local file" () {
        given:
            File testfile= new File(READROOT,'file3.txt')
            List<vfsURI> uris = [ new vfsURI( testfile ) ]
            Cmd cmd = new Cat( 'uris' : uris, suppressRepeatedEmptyLines : true )
            cmd.out = new PrintStream(capture)

        when:
            cmd.run(vfs)

        then:
            capture.toString() == new File(EXPECTEDROOT,'file3-suppressed-repeating-blanks.txt').text
    }

    def "Mark EOL in a local file" () {
        given:
            File testfile= new File(READROOT,'file1.txt')
            List<vfsURI> uris = [ new vfsURI( testfile ) ]
            Cmd cmd = new Cat( 'uris' : uris, showEndOfLines : true )
            cmd.out = new PrintStream(capture)

        when:
            cmd.run(vfs)

        then:
            capture.toString() == new File(EXPECTEDROOT,'file1-with-eol-markers.txt').text
    }

    def "Show tabs in a local file" () {
        given:
            File testfile= new File(READROOT,'file5.txt')
            List<vfsURI> uris = [ new vfsURI( testfile ) ]
            Cmd cmd = new Cat( 'uris' : uris, showTabs : true )
            cmd.out = new PrintStream(capture)

        when:
            cmd.run(vfs)

        then:
            capture.toString() == new File(EXPECTEDROOT,'file5-show-tabs.txt').text
    }

    def "Show non-printing characters (excluding LF and TAB) in a local file" () {
        given:
            File testfile= new File(READROOT,'file6.dat')
            List<vfsURI> uris = [ new vfsURI( testfile ) ]
            Cmd cmd = new Cat( 'uris' : uris, showNonPrinting : true )
            cmd.out = new PrintStream(capture)

        when:
            cmd.run(vfs)

        then:
            capture.toString() == new File(EXPECTEDROOT,'file6-show-non-printing.txt').text
    }
}