// ============================================================================
// (C) Copyright Schalk W. Cronje 2014
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================
package org.ysb33r.groovy.vfsplugin.cloud.core

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.Synchronized
import org.apache.commons.vfs2.Capability
import org.apache.commons.vfs2.FileNotFolderException
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystemException
import org.apache.commons.vfs2.FileSystemManager
import org.apache.commons.vfs2.FileType
import org.apache.commons.vfs2.FilesCache
import org.apache.commons.vfs2.provider.UriParser
import org.jclouds.blobstore.BlobStore
import org.jclouds.blobstore.domain.Blob
import org.apache.commons.vfs2.provider.AbstractFileSystem
import org.apache.commons.vfs2.provider.AbstractFileName
import org.apache.commons.vfs2.provider.AbstractFileObject
import org.jclouds.blobstore.domain.BlobMetadata
import org.jclouds.blobstore.domain.PageSet
import org.jclouds.blobstore.domain.StorageMetadata
import org.jclouds.blobstore.domain.StorageType
import org.jclouds.blobstore.options.ListContainerOptions

/**
 * A file in an SMB file system.
 */
@CompileStatic
abstract class AbstractCloudFileObject extends AbstractFileObject<AbstractCloudFileSystem>
{
    protected BlobStore blobStore
    protected Blob file
    protected boolean isContainerDirectory
    private File cachedFile

    protected AbstractCloudFileObject(final AbstractFileName name, final AbstractCloudFileSystem fileSystem,BlobStore bs) {
        super(name, fileSystem)
        blobStore = bs
    }

    abstract protected String getCloudContainerName()

    /** if Capability does not support RENAME then result is false, otherwise rename will be possible if
     * on same file susye,
     *
     * @param newfile
     */
    @Override
    boolean canRenameTo(final FileObject newfile) {
        if(fileSystem.hasCapability(Capability.RENAME)) {
            super.canRenameTo(newfile)
        } else {
            false
        }
    }

    /** Returns a full path appropriately decodes and dropping the leading forward slash
     * This is needed as some underlying cloud provider libraries need to have the full path
     * in order to create to create directories or files.
     *
     */
    protected String getCloudPathname() {
        name.pathDecoded[1..-1]
    }

    /** Returns true is this FileObject is the root of the file system
     *
     */
    protected boolean isRoot() {
        fileSystem.root == this
    }

    /**
     * Attaches this file object to its file resource.
     */
    @Override
    protected void doAttach()
    {
        if (file == null && !isRoot())
        {
            if(blobStore.blobExists(cloudContainerName,cloudPathname)) {
                file = blobStore.getBlob(cloudContainerName,cloudPathname)
            } else if(blobStore.directoryExists(cloudContainerName,cloudPathname)) {
                isContainerDirectory = true
            } else {
                file = blobStore.blobBuilder(cloudPathname).type(StorageType.BLOB).build()
            }
        }
    }

    @Override
    protected void doDetach() throws Exception
    {
        // file closed through content-streams ??
        file = null
        isContainerDirectory = null
    }

    /**
     * Creates an output stream to write the file content to.
     */
    @Override
    protected OutputStream doGetOutputStream(final boolean bAppend) throws Exception
    {
        // Create an OutputStream which will become an InputStream for the Blob
        assert isAttached()

        if(isContainerDirectory) {
            throw new FileNotFolderException ( name )
        }

        if(cachedFile == null) {
            cachedFile = File.createTempFile("vfs_blob.",".${name.scheme}")
            if(bAppend && exists()) {
                file.payload.openStream().withStream { InputStream is ->
                    cachedFile << is
                }
            }
        }

        new BlobOutputStream(cachedFile,this.file,bAppend, { Blob b ->
            blobStore.putBlob(cloudContainerName,b)
        } )
    }

    @Override
    protected FileType doGetType() {
        if(fileSystem.root==this)  {
            FileType.FOLDER
        } else if( blobStore.blobExists(cloudContainerName,cloudPathname) ) {
            FileType.FILE
        } else if ( blobStore.directoryExists(cloudContainerName,cloudPathname)) {
            FileType.FOLDER
        } else {
            FileType.IMAGINARY
        }
    }

    /**
     * Creates this file as a folder.
     */
    @Override
    protected void doCreateFolder() {
        blobStore.createDirectory(cloudContainerName,cloudPathname)
    }

    /**
     * Lists the children of the file.  Is only called if {@link #doGetType}
     * returns {@link FileType#FOLDER}.
     */
    @Override
    protected String[] doListChildren()
    {
       if( isRoot()  ) {
           handlePagingCloudList() { StorageMetadata it -> UriParser.encode(it.name) } as String []
       } else if( type == FileType.FOLDER ) {
           handlePagingCloudList((new ListContainerOptions()).inDirectory(cloudPathname)) { StorageMetadata it ->
                // This kind of filter can return the folder name itself, so filter it out
                def parts = it.name.split(/\//)
                parts.size() == 1 ? '' : UriParser.encode(parts.last())
           }.findAll { String it -> it.size() } as String []
        } else {
            [] as String []
        }
    }

    /**
     * Deletes the file.
     */
    @Override
    protected void doDelete() {
        if (isRoot()) {
            throw new FileSystemException('vfs.provider/delete.error', 'Cannot delete the root filesystem')
        }

        if (type == FileType.FOLDER) {
            blobStore.deleteDirectory(cloudContainerName,cloudPathname)
        } else if (type == FileType.FILE) {
            blobStore.removeBlob(cloudContainerName,cloudPathname)
        }
    }

    @Override
    protected void doRename(final FileObject newfile) {
        throw new FileSystemException("vfs.provider/rename-not-supported.error");
    }

    /**
     * Determines if this file is hidden.
     */
    @Override
    protected boolean doIsHidden() {
        false
    }

    /**
     * Returns the last modified time of this file.
     */
    @Override
    protected long doGetLastModifiedTime() {
        blobStore.blobMetadata(cloudContainerName,cloudPathname).lastModified.time
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    @Override
    protected long doGetContentSize()  {
        assert isAttached()
        if(type == FileType.FOLDER) {
            return 0
        } else {
            return file.payload.contentMetadata.contentLength as long
        }
    }

    /**
     * Creates an input stream to read the file content from.
     */
    @Override
    protected InputStream doGetInputStream() throws Exception
    {
        assert isAttached()
        if(type==FileType.FOLDER) {
            throw new FileNotFolderException(name)
        } else if( type != FileType.FILE ) {
            throw new org.apache.commons.vfs2.FileNotFoundException(name)
        } else {
            file.payload.openStream()
        }
    }

    /** When performing a list in the cloud, not all files will necessarily be returned. This operation
     * takes care of handling paged returns
     * @param opts
     * @param metadataHandler
     * @return
     */
    private def handlePagingCloudList( ListContainerOptions opts= new ListContainerOptions(), Closure metadataHandler ) {
        def children = []
        PageSet listing
        boolean done = false
        while( !done  ) {
            if(listing) {
                opts=opts.afterMarker(listing.nextMarker)
            }
            listing = blobStore.list(cloudContainerName,opts)
            children+= listing.collect(metadataHandler)
            done= listing.nextMarker == null || listing.nextMarker.size()
        }
        return children
    }
}
