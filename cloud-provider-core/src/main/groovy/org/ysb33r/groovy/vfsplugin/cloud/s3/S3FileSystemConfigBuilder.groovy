package org.ysb33r.groovy.vfsplugin.cloud.s3

import groovy.transform.CompileStatic
import org.ysb33r.groovy.vfsplugin.cloud.core.AbstractCloudFileSystemConfigBuilder

/**
 * Created by schalkc on 08/05/2014.
 */
@CompileStatic
class S3FileSystemConfigBuilder extends AbstractCloudFileSystemConfigBuilder {

    static final S3FileSystemConfigBuilder instance = new S3FileSystemConfigBuilder()

    private S3FileSystemConfigBuilder() { super('s3.') }
    protected S3FileSystemConfigBuilder(final String prefix) { super(prefix) }

    @Override
    protected Class<? extends FileSystem> getConfigClass() { S3FileSystem.class }

}
