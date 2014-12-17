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
import org.ysb33r.groovy.dsl.vfs.FileActionException

class MvSpec extends Specification {

    static final File COPYROOT= new File("${System.getProperty('TESTFSREADROOT')}/test-files")
    static final File SRCROOT= new File("${System.getProperty('TESTFSWRITEROOT')}/test-files/mv-source")
    static final File DESTROOT= new File("${System.getProperty('TESTFSWRITEROOT')}/test-files/mv-dest")

    def vfs = new VFS()

    void setup() {
        [ SRCROOT, DESTROOT ].each {
            if(it.exists()) {
                it.deleteDir()
            }
            it.mkdirs()
        }

        vfs {
            cp COPYROOT, SRCROOT, recursive:true, overwrite:false , filter: exclude_self
        }
    }

    void cleanup() {
    }

    def "Plain move (no options) should move the file" () {
        given:
            File from= new File(SRCROOT,'file1.txt')
            File to=   new File(DESTROOT,'file1.txt')
            List<vfsURI> uris = [ new vfsURI( from )  ]
            Mv cmd = new Mv( sources : uris, destination : new vfsURI( to.parentFile ) )

        when:
            cmd.run(vfs)

        then:
            !from.exists()
            to.exists()
    }

    def "'mv --no-clobber uri1 uri2' should fail if uri2 exists"() {
        given:
            File from= new File(SRCROOT,'file1.txt')
            File to=   new File(DESTROOT,'file1.txt')
            List<vfsURI> uris = [ new vfsURI( from )  ]
            Mv cmd = new Mv(
                    sources : uris,
                    destination : new vfsURI( to.parentFile ),
                    overwrite : false
            )

        when:
            vfs { cp new File(SRCROOT,'file2.txt'), to }
            cmd.run(vfs)

        then:
            thrown(FileActionException)
            from.exists()


    }

    def "'mv --force uri1 uri2' should succeed if uri2 exists"() {
        given:
            File from= new File(SRCROOT,'file1.txt')
            File to=   new File(DESTROOT,'file1.txt')
            File seed= new File(SRCROOT,'file2.txt')
            List<vfsURI> uris = [ new vfsURI( from )  ]
            Mv cmd = new Mv(
                    sources : uris,
                    destination : new vfsURI( to.parentFile ),
                    overwrite : true
            )

        when:
            vfs { cp seed, to }
            cmd.run(vfs)

        then:
            to.exists()
            !from.exists()
            to.text != seed.text
    }

    def "Cannot move a file if destination is specified as a file, but destination is a folder"() {
        given:
            File from= new File(SRCROOT,'file1.txt')
            File to=   new File(DESTROOT,'file1.txt')
            Mv cmd = new Mv(
                    sources : [ new vfsURI( from )  ],
                    destination : new vfsURI( DESTROOT ),
                    overwrite : true,
                    targetIsFile : true
            )

        when:
            cmd.run(vfs)

        then:
            thrown(FileActionException)
    }

    def "Cannot move a folder if destination is specified as a file"() {
        given:
            File to=   new File(DESTROOT,'file1.txt')
            Mv cmd = new Mv(
                    sources : [ new vfsURI( SRCROOT ) ],
                    destination : new vfsURI( to ),
                    overwrite : true,
                    targetIsFile : true
            )

        when:
            cmd.run(vfs)

        then:
            thrown(FileActionException)
    }

    def "Can move a file if destination is specified as a file and destination exists" () {
        given:
        File from= new File(SRCROOT,'file1.txt')
        File to=   new File(DESTROOT,'file1.txt')
        Mv cmd = new Mv(
                sources : [ new vfsURI( from )  ],
                destination : new vfsURI( to ),
                overwrite : true,
                targetIsFile : true
        )

        when:
            vfs { cp from, to }
            cmd.run(vfs)

        then:
            !from.exists()
            to.exists()
    }

    def "Can move a file if destination is specified as a file and destination does not exist" () {
        given:
            File from= new File(SRCROOT,'file1.txt')
            File to=   new File(DESTROOT,'file1.txt')
            Mv cmd = new Mv(
                    sources : [ new vfsURI( from )  ],
                    destination : new vfsURI( to ),
                    overwrite : true,
                    targetIsFile : true
            )

        when:
            cmd.run(vfs)

        then:
            !from.exists()
            to.exists()
    }

    def "When file is moved, but intermediaries do not exist, throw an exception"() {
        given:
            File from= new File(SRCROOT,'test-subdir/file3.txt')
            File to=   new File( DESTROOT,'test-subdir/file3.txt')
            Mv cmd = new Mv(
                    sources : [ new vfsURI( from )  ],
                    destination : new vfsURI( DESTROOT ),
                    overwrite : true,
                    targetIsFile : true
            )
            assert from.exists()
            assert !to.exists()

        when:
            cmd.run(vfs)

        then:
            thrown(FileActionException)
            from.exists()
    }

    def "When file is moved and intermediates is true, but intermediaries do not exist, create intermediaries"() {
        given:
            File from= new File(SRCROOT,'test-subdir/file3.txt')
            File to=   new File( DESTROOT,'test-subdir/file3.txt')
            Mv cmd = new Mv(
                    sources : [ new vfsURI( from )  ],
                    destination : new vfsURI( to ),
                    overwrite : true,
                    targetIsFile : true,
                    intermediates : true
            )
            assert from.exists()
            assert !to.exists()

        when:
            cmd.run(vfs)

        then:
            !from.exists()
            to.exists()

    }

}