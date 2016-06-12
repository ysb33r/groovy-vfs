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
        '//foo'          || 1
        '/foo//bar//zoo' || 3
        'foo'            || 1
        'foo//bar'       || 2
        'foo/bar'        || 2
        'foo/bar/zoo'    || 3

    }

    @Unroll
    def "Getting a part of a path as a new path"() {
        when: "The path is '#input'"
        def path = new TestPosixPath(input)

        then: "Then index #index is '#output'"
        path.getName(index) == new TestPosixPath(output)

        where:
        input      | index || output
        '/foo/bar' | 0     || '/foo'
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
        def parent = parentStr == null ? null : new TestPosixPath(parentStr)

        then: "It's parent is #parentStr"
        parent == path.parent

        where:
        pathStr    || parentStr
        '/foo'     || '/'
        'foo/bar'  || 'foo'
        '/foo/bar' || '/foo'
        '/'        || null
    }

    @Unroll
    def "A subpath creates a relative path"() {

        when: "A path consisting of '#pathStr'"
        def path = new TestPosixPath(pathStr)
        def newPath = path.subpath(start,end)

        then: "With subpath(#begin,#end) yields '#newStr'"
        newPath.absolute == false
        newPath == new TestPosixPath(newStr)

        where:
        pathStr      | start | end || newStr
        '/a/b/c/d/e' | 0     | 2   || 'a/b/c/d/e'
        '/a/b/c/d/e' | 0     | 2   || 'a/b/c'
        'a/b/c/d/e'  | 0     | 2   || 'a/b/c'
        'a/b/c/d/e'  | 1     | 3   || 'b/c/d'
        'a/b/c/d/e'  | 3     | 4   || 'c/d'
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
        5  |  'out of bounds'
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
        sibling.absolute == path.absolute
        sibling.toString() == new TestPosixPath(expected).toString()

        where:
        input           | other  || expected
        '/a/b/c'        | 'd/e'  || '/a/b/d/e'
        'a/b/c'         | 'd/e'  || 'a/b/d/e'
        '/a/b/c'        | '/d/e' || '/d/e'
        'a/b/c'         | '/d/e' || '/d/e'
        '/'             | 'd/e'  || 'd/e'
        '/'             | '/d/e' || '/d/e'
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
        path.toUri() == "${TestPosixPath.BASE_URI}/${expected}".toURI()

        where:
        input               || expected
        '/a/b/c/d/e'        || 'a/b/c/d/e'
        'b/c/d'             || 'b/c/d'

    }

    static final NULLFS = new NullFileSystem()
    // Expecting the body of this class to shrink

    @EqualsAndHashCode
    static class TestPosixPath extends AbstractPosixPath<NullFileSystem> {

        final static URI BASE_URI = new URI('test','authority',null,null,null)

        TestPosixPath(String path, String... more) {
            super(NULLFS, BASE_URI,path, more)
        }

        TestPosixPath() {
            super(NULLFS,BASE_URI)
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
        Path relativize(Path other) {
            return null
        }

        @Override
        Path toRealPath(LinkOption... options) throws IOException {
            return null
        }

        @Override
        WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
            return null
        }

        @Override
        WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
            return null
        }

        @Override
        Iterator<Path> iterator() {
            return null
        }

    }
}