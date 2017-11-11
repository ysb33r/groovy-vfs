package org.ysb33r.vfs.core.helpers

import org.ysb33r.vfs.core.VfsEngine
import spock.lang.Specification

class CoreBaseSpecification extends Specification {

    static final File testFsReadOnlyRoot = new File(System.getProperty('TESTFSREADROOT') ?: './dsl/groovy-vfs','src/test/resources/test-files').absoluteFile
    static private final File testFsWriteRoot = new File( System.getProperty('TESTFSWRITEROOT') ?: './dsl/groovy-vfs/build','tmp/test-files').absoluteFile

    static File identifyWriteRoot(final String name) {
        new File(testFsWriteRoot,name)
    }
//    static final String testFsURI = testFsReadOnlyRoot.toURI().toString()
//    static final String testFsWriteURI = testFsWriteRoot.toURI()..toString()

    void setup() {
        if(testFsWriteRoot.exists()) {
            testFsWriteRoot.deleteDir()
        }
        testFsWriteRoot.mkdirs()
    }

    VfsEngine setupVfs() {
        VfsEngineBuilder.build('Standard Vfs Test')
    }

}