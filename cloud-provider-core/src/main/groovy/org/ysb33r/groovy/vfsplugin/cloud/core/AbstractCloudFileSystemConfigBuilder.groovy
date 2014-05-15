package org.ysb33r.groovy.vfsplugin.cloud.core

import org.apache.commons.vfs2.FileSystemConfigBuilder

/**
 * Created by schalkc on 08/05/2014.
 */
abstract class AbstractCloudFileSystemConfigBuilder extends FileSystemConfigBuilder {
    protected AbstractCloudFileSystemConfigBuilder(final String prefix) { super(prefix) }
}
