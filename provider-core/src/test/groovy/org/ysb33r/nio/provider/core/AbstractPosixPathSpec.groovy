/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2016
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
package org.ysb33r.nio.provider.core

import groovy.transform.EqualsAndHashCode
import org.ysb33r.nio.provider.core.helpers.NullFileSystem
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService


class AbstractPosixPathSpec extends Specification {


    def "A Posix Path is always associated with a filesystem type"() {

        when: "A filesystem is requested"
        def path = new TestPosixPath()

        then: "The filesystem that ctrated the path is returned"
        path.fileSystem == NULLFS
    }

    def "By default a Posix Path is not linked to a File object"() {

        when: "A Posix Path is converted to a File"
        def path = new TestPosixPath()
        path.toFile()

        then: "An UnsupportedOperationException is thrown"
        thrown(UnsupportedOperationException)
    }

    @Unroll
    def "#type paths #type2 start with '/'"() {

        when: "A path is created with '#abs'"
        def path = new TestPosixPath(pathStr)

        then: "It is considered: #type"
        path.isAbsolute() == abs

        where:
        type | type2 | pathStr || abs
        'Absolute' | '' | '/foo/bar' || true
        'Relative' | 'do not' | 'foo/bar' || false

    }

    @Unroll
    def "'//' does not increase name count"() {

        when: 'A path contains #pathStr'
        def path = new TestPosixPath(pathStr)

        then: 'It has #count parts'
        path.nameCount == count

        where:
        pathStr          || count
        '//foo'          || 2
        '/foo//bar//zoo' || 3
        'foo'            || 1
        'foo//bar'       || 2
        'foo/bar'        || 2
        'foo/bar/zoo'    || 4

    }

    @Unroll
    def "Getting a part of a path (#input) as a new path"() {
        when: "The path is '#input'"
        def path = new TestPosixPath(input)

        then: "Then index #index is '#output'"
        path.getName(index) == new TestPosixPath(output)

        where:
        input      | index || output
        '/foo/bar' | 0     || 'foo'
        '/foo/bar' | 1     || 'bar'
        'foo/bar'  | 0     || 'foo'

    }

    @Unroll
    def "#description index (#index) cannot be used to obtain a new path"() {

        when: "A path consists of two parts"
        def path = new TestPosixPath('/foo/bar')
        path.getName(index)

        then: "Using #index will emit an exception"
        thrown(IllegalArgumentException)

        where:
        index | description
        -1 | 'Negative'
        5 |  'Out of bounds'
        2 |  'Out of bounds'

    }

    def "The root of an absolute path is '/'"() {
        when:
        def path = new TestPosixPath('/foo/bar')

        then:
        path.root == new TestPosixPath('/')
    }

    def "A relative path does not have a root path"() {
        when:
        def path = new TestPosixPath('foo/bar')

        then:
        path.root == null

    }

    @Unroll
    def "A parent path is the path above the current path, but roots do not have parents"() {
        when: "A path is '#pathStr'"
        def path = new TestPosixPath(pathStr)
        def expected = expectedParentStr == null ? null : new TestPosixPath(expectedParentStr)

        then: "It's parent is #expectedParentStr"
        path.parent == expected

        where:
        pathStr    || expectedParentStr
        '/foo'     || '/'
        'foo/bar'  || 'foo'
        '/foo/bar' || '/foo'
        '/fo/ba/c' || '/fo/ba'
        'fo/ba/c'  || 'fo/ba'
        '/'        || null
    }

    @Unroll
    def "A subpath creates a relative path: #pathStr(#start,#end)"() {

        when: "A path consisting of '#pathStr'"
        def path = new TestPosixPath(pathStr)
        def newPath = path.subpath(start,end)

        then: "With subpath(#begin,#end) yields '#newStr'"
        newPath.absolute == false
        newPath == new TestPosixPath(newStr)

        where:
        pathStr      | start | end || newStr
        '/a/b/c/d/e' | 0     | 5   || 'a/b/c/d/e'
        '/a/b/c/d/e' | 0     | 2   || 'a/b'
        '/a/b/c/d/e' | 0     | 2   || 'a/b'
        'a/b/c/d/e'  | 0     | 2   || 'a/b'
        'a/b/c/d/e'  | 1     | 3   || 'b/c'
        'a/b/c/d/e'  | 3     | 4   || 'd'
        'a/b/c/d/e'  | 1     | 4   || 'b/c/d'
    }

    @Unroll
    def "#description begin index (#index) cannot be used to obtain a new subpath"() {

        when: "A path consists of five parts"
        def path = new TestPosixPath('/a/b/c/d/e')
        path.subpath(index,3)

        then: "Using #index will emit an exception"
        thrown(IllegalArgumentException)

        where:
        index | description
        -1 | 'Negative'
        5 |  'Out of bounds'
        9 |  'Out of bounds'
    }

    @Unroll
    def "End index (#index) cannot be used to obtain a new subpath if #description "() {

        when: "A path consisting of nultiple parts"
        def path = new TestPosixPath('/a/b/c/d/e')
        path.subpath(2,index)

        then: "Using #index will emit an exception"
        thrown(IllegalArgumentException)

        where:
        index | description
        -1 | 'negative'
        6  |  'out of bounds (+1)'
        9  |  'out of bounds'
        1  |  'less than or equal to start index'
    }

    @Unroll
    def "Converting path ('#input','#input2') to string "() {
        when: "'#input' + '#input2' are used to construct path"
        def path = new TestPosixPath(input,input2)

        then: "the output is expected to '#output'"
        path.toString() == output

        where:
        input | input2    || output
        '/a/b'| '/c/d/e'  || '/a/b/c/d/e'
        'a/b' | 'c/d/e'   || 'a/b/c/d/e'

    }

    @Unroll
    def "Check whether one path starts with another"() {

        when: "The path is '#input"
        def path = new TestPosixPath(input)
        def check = new TestPosixPath(other)

        then: "when it is checked against '#output' the match should report: #result"
        path.startsWith(check) == result

        where:
        input         | other         || result
        '/a/b/c/d/e'  | '/a/b/c/d/e'  || true
        '/a/b/c/d/e'  | '/a/b/c/d/e/' || true
        '/a/b/c/d/e/' | '/a/b/c/d/e'  || true
        '/a/b/c/d/e'  | '/a/b'        || true
        '/a/b/c/d/e'  | 'a/b'         || false
        'a/b/c/d'     | '/a/b'        || false
        'a/b/c'       | 'a'           || true
        'a/b/c'       | 'a/b'         || true
        'a/b'         | 'a/b/c'       || false
    }

    @Unroll
    def "Check whether one path ends with another"() {

        when: "The path is '#input"
        def path = new TestPosixPath(input)
        def check = new TestPosixPath(other)

        then: "when it is checked against '#output' the match should report: #result"
        path.endsWith(check) == result

        where:
        input         | other         || result
        '/a/b/c/d/e'  | '/a/b/c/d/e'  || true
        '/a/b/c/d/e'  | '/a/b/c/d/e/' || true
        '/a/b/c/d/e/' | '/a/b/c/d/e'  || true
        '/a/b/c/d/e'  | '/a/b'        || false
        '/a/b/c/d/e'  | 'a/b'         || false
        'a/b/c/d'     | 'a/b/c/d'     || true
        'a/b/c'       | 'c'           || true
        'a/b/c'       | 'b/c'         || true
        'a/b'         | 'a/b/c'       || false
    }

    def "Paths on different filesystem can not be compared"() {
        given: 'A path on test file system and a path of the default filesystem with matching strings'
        def path = new TestPosixPath('/a/b/c')
        def other = new File('/a/b/c').toPath()

        expect: 'startsWith will always return false'
        path.startsWith(other) == false

        and: 'endsWith will always return false'
        path.endsWith(other) == false
    }

    @Unroll
    def "Normalised paths eliminate parent & current directory aliases"() {

        when: "Path '#input' is normalised"
        def path = new TestPosixPath(input)
        def normalized = path.normalize()

        then: "It is '#expected'"
        normalized == new TestPosixPath(expected)
        normalized.toString() == new TestPosixPath(expected).toString()
        normalized.absolute == absolute

        where:
        input               || expected     | absolute
        '/a/b/c/d/e'        || '/a/b/c/d/e' | true
        '/a/b/./d/e'        || '/a/b/d/e'   | true
        '/a/b/../d/e/'      || '/a/d/e'     | true
        'a//b//c//..//../e' || 'a/e'        | false
        'a/b/c/d'           || 'a/b/c/d'    | false
        './a/b/c/d'         || 'a/b/c/d'    | false
        '/./a/b/c/d'        || '/a/b/c/d'   | true
        'a/b/c/d/.'         || 'a/b/c/d'    | false
    }

    @Unroll
    def "Creating absolute path for #input will normalize and resolve against a given root"() {

        when: "The original path is '#input'"
        def path = new TestPosixPath(input)
        def absPath = path.toAbsolutePath()

        then: "It's absolute equivalent is '#expected'"
        absPath.absolute == true
        absPath.toString() == new TestPosixPath(expected).toString()

        where:
        input               || expected
        '/a/b/c/d/e'        || '/a/b/c/d/e'
        '/a/b/./d/e'        || '/a/b/d/e'
        '/a/b/../d/e/'      || '/a/d/e'
        'a//b//c//..//../e' || '/a/e'
        'a/b/c/d'           || '/a/b/c/d'
        './a/b/c/d'         || '/a/b/c/d'
        '/./a/b/c/d'        || '/a/b/c/d'
        'a/b/c/d/.'         || '/a/b/c/d'
    }

    @Unroll
    def "Resolve sibling path for #other against #input"() {

        when: "A a path is resolved as as sibling"
        def path = new TestPosixPath(input)
        def sibling = path.resolveSibling(other)

        then: "It's absolute equivalent is '#expected'"
        sibling.toString() == new TestPosixPath(expected).toString()

        and: "If this original parent path was absolute, the result should be absolute"
        sibling.absolute == absolute

        where:
        input           | other  || expected    | absolute
        '/a/b/c'        | 'd/e'  || '/a/b/d/e'  | true
        'a/b/c'         | 'd/e'  || 'a/b/d/e'   | false
        '/a/b/c'        | '/d/e' || '/d/e'      | true
        'a/b/c'         | '/d/e' || '/d/e'      | true
        '/'             | 'd/e'  || 'd/e'       | false
        '/'             | '/d/e' || '/d/e'      | true
    }

    def "Empty source paths with always return the target in sibling resolve"() {
        when: "The path is empty"
        def path = new TestPosixPath()
        def sibling = path.resolveSibling('/d/e')

        then: "The target path is returned"
        sibling.toString() == '/d/e'
    }

    @Unroll
    def "Creating a URI from #input adds a scheme & authority and make the path absolute"() {

        when: "A path is '#input'"
        def path = new TestPosixPath(input)

        then: "The URI is like SCHEME://AUTHORITY/#expected"
        path.toUri() == "test://authority/${expected}".toURI()

        where:
        input               || expected
        '/a/b/c/d/e'        || 'a/b/c/d/e'
        'b/c/d'             || 'b/c/d'

    }

    @Unroll
    def "Absolute paths sare deterministic on a POSIX-like system"() {

        when:
        Path relative = new TestPosixPath(path1).relativize(new TestPosixPath(path2))
        Path resolved = new TestPosixPath(path1).resolve(relative).normalize()

        then:
        relative.absolute == false
        relative.toString() == new TestPosixPath(result).toString()
        resolved.toString() == new TestPosixPath(path2).toString()

        where:
        path1      | path2      || result
        '/a/b'     | '/a/b/e/f' || 'e/f'
        '/a/b/c/d' | '/a/b/e/f' || '../../e/f'
        'a/b'      | 'a/b/e/f'  || 'e/f'
        'a/b/c/d'  | 'a/b/e/f'  || '../../e/f'
        '/a/b/c'   | '/a/b'     || '..'
    }

    def "Normalised path check for relative paths"() {
        given: "Two normalised paths of which one is absolute and the second is relative"
        def path1 = new TestPosixPath('/a/b/c/d').normalize()
        def path2 = new TestPosixPath('c/d').normalize()

        when: "The relative path is resolved against the absolute path"
        def resolved = path1.resolve(path2)

        and: "This resolved path is taken relative to the original absolute path"
        def relative = path1.relativize(resolved)

        then: "The results is the original relative path"
        relative == path2
    }

    @Unroll
    def "Relative path is empty when #reason"() {
        given: "Source path is '#path1'"
        def path1 = new TestPosixPath(pathStr1)
        def path2 = new TestPosixPath(pathStr2)

        when: "Determine relative path to '#path2'"
        Path result = path1.relativize(path2)

        then: "The resulting path is empty"
        result.nameCount == 0

        where:
        pathStr1 | pathStr2 | reason
        '/a/b/c' | '/a/b/c' | 'both paths are the same and is absolute'
        '/a/b/c' | 'b/c'    | 'one path is absolute and the other is relative'
        'a/b/c'  | '/b/c'   | 'one path is absolute and the other is relative'
        '/c/d/e' | '/a'     | 'there are no common root parts'
    }

    def "Paths need to be on same filesystem in order to determine relative paths"() {

        given: 'Two paths on different filesystems'
        def path1 = new TestPosixPath('/a/b/c')
        def path2 = new File('/a/d/e').toPath()

        when: 'Relative path calculations are attempted'
        path1.relativize(path2)

        then: "An exception is emitted"
        thrown(IllegalArgumentException)
    }

    def "Iterating over path returns segments"() {
        given: "A path with three parts"
        def path1 = new TestPosixPath('/a/b/c')

        when: "Applying a collection operation"
        def parts = path1.collect{ it.toString()}

        then: "Will iterate over all the parts"
        parts == ['a','b','c']

        when: "Iterating over end of parts will throw exception"
        Iterator itr = path1.iterator()
        int correctIterations= 0
        (1..4).each {itr.next(); correctIterations++}

        then:
        thrown(NoSuchElementException)
        correctIterations == 3

    }

    static final NULLFS = new NullFileSystem()
    // Expecting the body of this class to shrink

    @EqualsAndHashCode
    static class TestPosixPath extends AbstractPosixPath<NullFileSystem> {

        TestPosixPath(String path, String... more) {
            super(NULLFS, 'authority',path, more)
        }

        TestPosixPath() {
            super(NULLFS,'authority')
        }

        @Override
        protected String getScheme() { 'test'}

        @Override
        protected Path createPath(String part, String... more) {
            new TestPosixPath(part,more)
        }

        @Override
        protected Path createPath() {
            new TestPosixPath()
        }

        @Override
        Path toRealPath(LinkOption... options)  {
            toAbsolutePath()
        }

        @Override
        WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
            return null
        }

        @Override
        WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
            return null
        }

    }
}