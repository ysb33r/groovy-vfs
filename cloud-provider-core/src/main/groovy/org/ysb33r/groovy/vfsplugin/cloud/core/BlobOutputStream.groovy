package org.ysb33r.groovy.vfsplugin.cloud.core

import groovy.transform.CompileStatic
import org.apache.commons.vfs2.util.MonitorOutputStream
import org.jclouds.blobstore.BlobStore
import org.jclouds.blobstore.domain.Blob

/**
 * Created by schalkc on 12/05/2014.
 */
@CompileStatic
class BlobOutputStream extends MonitorOutputStream {

    private Blob blob
    private Closure updater
    private File cachedFile

    BlobOutputStream(File tmpFile,Blob bs,final boolean append, final Closure upd) {
        super(new FileOutputStream(tmpFile,append))
        blob = bs
        cachedFile = tmpFile
        updater = upd
    }

    @Override
    protected void onClose()  {
        blob.setPayload(cachedFile)
        updater(blob)
    }

}
