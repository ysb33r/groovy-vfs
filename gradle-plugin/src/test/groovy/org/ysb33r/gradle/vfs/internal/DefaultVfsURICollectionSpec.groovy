package org.ysb33r.gradle.vfs.internal

import org.gradle.api.Project
import org.gradle.api.tasks.StopExecutionException
import org.gradle.testfixtures.ProjectBuilder
import org.ysb33r.gradle.vfs.VfsURI
import org.ysb33r.gradle.vfs.VfsURICollection
import org.ysb33r.groovy.dsl.vfs.VFS
import spock.lang.Shared
import spock.lang.Specification


/** Although this is a DefaultResolvedURICollection - we test it purely using the interface functions
 *
 * @author Schalk W. Cronjé
 */
class DefaultVfsURICollectionSpec extends Specification {

    static final File TESTFSREADONLYROOT  = new File("${System.getProperty('TESTFSREADROOT') ?: '.'}/src/test/resources/test-archives")

    @Shared Project project = ProjectBuilder.builder().build()
    @Shared String uriRootResolved
    @Shared VfsURI uri1
    @Shared VfsURI uri2
    @Shared VfsURI unresolvableURI
    @Shared VFS vfs

    VfsURICollection uris

    void setupSpec() {
        vfs = new VFS()
        uriRootResolved= vfs.resolveURI(TESTFSREADONLYROOT).toString()
        uri1 = UriUtils.uriWithOptions([:], vfs,"tbz2:${uriRootResolved}/test-files.tar.bz2!file1.txt")
        uri2 = UriUtils.uriWithOptions([:], vfs,"tbz2:${uriRootResolved}/test-files.tar.bz2!file2.txt")
        unresolvableURI = UriUtils.uriWithOptions([:], vfs,"ftp://non.existing.host/test-files.tar.bz2")
    }

    void setup() {
        uris = UriUtils.emptyURICollection()
    }

    def "An empty collection should return empty"() {
        expect:
            uris.isEmpty()
    }

    def "Have the ability to stop when collection is empty"() {
        when: "Trying to return an empty collection via stop"
            uris.stopExecutionIfEmpty()

        then: "Should throw exception is empty"
            thrown (StopExecutionException)

        when: "A URI is present"
            uris.add(uri1)

        then: "Trying to return the colelct should not raise an exception"
            uris.stopExecutionIfEmpty() == uris
    }

    def "If a URI is added, expected it to be contained" () {
        when:
            def collection = uris.add(uri1)

        then: "The same collection object needs to be returned"
            collection == uris

        and: "it should contain the added URI"
            uris.contains(uri1)

        and: "it should not contain any other URI"
            !uris.contains(uri2)
    }

    def "Good URIs need to be resolvable"() {
        given:
            def resolvedURI = uri2.resolve()
            assert resolvedURI.resolved
            assert resolvedURI instanceof ResolvedURI

        when: "Adding a resolved URI"
            uris.add(resolvedURI)

        then: "Collection should show as resolved"
            uris.allResolved()

        when: "Adding a staged URI and a resolved URI"
            uris.add(uri1)

        then: "Collection must show as not completely resolved"
            !uris.allResolved()

        when: "Collection is told to resolve"
            uris.resolve()

        then: "Collection must show as resolved"
            uris.allResolved()
    }

    def "Unresolvable staged URIs added to a collection must cause an exception when told to resolve"() {
        when: "Adding a unresolvable staged URI to a collection"
            uris.add(unresolvableURI)

        then: "It should be allowed"
            !uris.isEmpty()

        when: "Telling the collection to resolve and unresolvable URI "
            uris.resolve()

        then: "Resolving should cause an exception and collection should remain unresolved"
            thrown(Exception)
            !uris.allResolved()
    }

    def "Requesting all URIs will cause them to be resolved" () {
        when:
            uris.add(uri1)
            uris.add(uri2)
            assert !uris.allResolved()
            def resolved = uris.getUris()

        then:
            uris.allResolved()
            resolved.size() == 2
    }

    def "Must be able to iterate using the standard Groovy techniques"() {
        when:
            uris.add(uri1)
            uris.add(uri2)

        then:
            uris.each { }
            (uris.collect { it }) .size() == 2
            null != uris.find { it instanceof StagedURI }
    }
}