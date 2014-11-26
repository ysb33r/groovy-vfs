package org.ysb33r.groovy.vfsplugin.cloud.core

import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.operations.AbstractFileOperation

/**
 * Created by schalkc on 15/05/2014.
 */
class AbstractCloudFileOperation extends AbstractFileOperation {

    AbstractCloudFileOperation(FileObject file) { super(file) }

    void process() {

    }
}
