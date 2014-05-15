package org.ysb33r.groovy.vfsplugin.cloud.s3

import groovy.transform.CompileStatic
import org.apache.commons.vfs2.FileSystemException

/**
 * Created by schalkc on 08/05/2014.
 */
@CompileStatic
class S3FileNameException extends FileSystemException {

    S3FileNameException( final String s ) {
        super('vfs.provider.s3',s)
    }
}
