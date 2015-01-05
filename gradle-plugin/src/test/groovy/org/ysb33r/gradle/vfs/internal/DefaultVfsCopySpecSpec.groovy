

package org.ysb33r.gradle.vfs.internal

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.ysb33r.gradle.vfs.VfsProxy
import org.ysb33r.gradle.vfs.VfsCopySpec
import org.ysb33r.groovy.dsl.vfs.VFS
import spock.lang.Shared
import spock.lang.Specification


/**
 * @author Schalk W. Cronjé
 */
class DefaultVfsCopySpecSpec extends Specification {

    static final File TESTFSREADONLYROOT  = new File("${System.getProperty('TESTFSREADROOT') ?: '.'}/src/test/resources/test-archives")

    @Shared Project project
    @Shared VFS vfs
    @Shared String uriRootResolved

    VfsCopySpec copySpec
    String uri1

    void setupSpec() {
        project =  ProjectBuilder.builder().build()
        vfs = VfsProxy.request(project)
        uriRootResolved= vfs.resolveURI(TESTFSREADONLYROOT).toString()
    }

    void setup() {
        uri1 = "tbz2:${uriRootResolved}/test-files.tar.bz2!file1.txt"
        copySpec = DefaultVfsCopySpec.create(vfs) {
            from uri1
            into 'foo'
        }
    }

    def "Simple spec attributes"() {
        given:
            def collection = copySpec.uriCollection

        expect:
            copySpec.relativePath == 'foo'
            copySpec.children().empty
            !collection.empty
    }
}