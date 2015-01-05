

package org.ysb33r.gradle.vfs.internal

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.ysb33r.gradle.vfs.VfsCopySource
import org.ysb33r.gradle.vfs.VfsCopySpec
import org.ysb33r.gradle.vfs.VfsOptions
import org.ysb33r.gradle.vfs.VfsProxy
import org.ysb33r.gradle.vfs.VfsURI
import org.ysb33r.gradle.vfs.VfsURICollection
import org.ysb33r.groovy.dsl.vfs.URI
import org.ysb33r.groovy.dsl.vfs.VFS
import spock.lang.Shared
import spock.lang.Specification


/**
 * @author Schalk W. Cronjé
 */
class UriUtilsSpec extends Specification {

    static final File TESTFSREADONLYROOT  = new File("${System.getProperty('TESTFSREADROOT') ?: '.'}/src/test/resources/test-archives")

    @Shared Project project
    @Shared VFS vfs
    @Shared String uriRootResolved

    String uri1
    Closure uri2

    void setupSpec() {
        project =  ProjectBuilder.builder().build()
        vfs = VfsProxy.request(project)
        uriRootResolved= vfs.resolveURI(TESTFSREADONLYROOT).toString()
    }

    void setup() {
        uri1 = "tbz2:${uriRootResolved}/test-files.tar.bz2!file1.txt"
        uri2 = {"tbz2:${uriRootResolved}/test-files.tar.bz2!file2.txt"}
    }

    def "Create VfsURI from object"() {
        given:
            VfsURI vfsUri = UriUtils.uriWithOptions([:],vfs,uri1)

        expect:
            vfsUri.uri instanceof URI
            vfsUri.uri == new URI(uri1)
            vfsUri.uri.properties == [:]
            vfsUri.praxis == [:]
    }

    def "Create collection from list of objects"() {
        given:
        List<Object> uris= [uri1,uri2]
        VfsURICollection vfsCollection = UriUtils.uriWithOptions([:],vfs,uris)

        expect:
        vfsCollection instanceof DefaultVfsURICollection
        !vfsCollection.empty
    }

    def "Create collection from list of VfsCopySource instances"() {
        given:
            def copySource= new VfsCopySource() {
                @Override
                Object getSource() { uri1 }

                @Override
                VfsOptions getOptions() {
                    new VfsOptions() {
                        @Override
                        Map<String, Object> getOptionMap() { [ overwrite:true ] }
                    }
                }
            }
            VfsURICollection vfsCollection = UriUtils.uriWithOptions(vfs,[copySource])

        expect:
            vfsCollection instanceof DefaultVfsURICollection
            !vfsCollection.empty
    }
}
