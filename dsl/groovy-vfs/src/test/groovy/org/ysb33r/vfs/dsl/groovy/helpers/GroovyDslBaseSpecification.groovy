package org.ysb33r.vfs.dsl.groovy.helpers

import org.ysb33r.vfs.dsl.groovy.Vfs
import spock.lang.Specification

class GroovyDslBaseSpecification extends Specification {

    static final File testFsReadOnlyRoot =      new File(
        System.getProperty('TESTFSREADROOT') ?: 'core/src/test/resources/test-files'
    ).absoluteFile
    static private final File testFsWriteRoot = new File(
        System.getProperty('TESTFSWRITEROOT') ?: 'dsl/groovy-vfs/build/tmp/test-files',
        'groovy-vfs'
    ).absoluteFile

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