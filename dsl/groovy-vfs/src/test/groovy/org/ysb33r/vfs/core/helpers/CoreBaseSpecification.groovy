package org.ysb33r.vfs.dsl.groovy.helpers

import org.ysb33r.vfs.dsl.groovy.Vfs
import spock.lang.Specification

class GroovyDslBaseSpecification extends Specification {

    static final File testFsReadOnlyRoot = new File("${System.getProperty('TESTFSREADROOT')}/src/test/resources/test-files")
    static final File testFsWriteRoot = new File( "${System.getProperty('TESTFSWRITEROOT') ?: 'build/tmp/test-files'}/file")
//    static final String testFsURI = testFsReadOnlyRoot.toURI().toString()
//    static final String testFsWriteURI = testFsWriteRoot.toURI()..toString()

    void setup() {
        if(testFsWriteRoot.exists()) {
            testFsWriteRoot.deleteDir()
        }
        testFsWriteRoot.mkdirs()
    }

    Vfs setupVfs() {
        VfsBuilder.build('Standard Vfs Test')
    }

}