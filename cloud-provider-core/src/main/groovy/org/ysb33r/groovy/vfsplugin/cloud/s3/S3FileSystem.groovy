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
package org.ysb33r.groovy.vfsplugin.cloud.s3

import groovy.transform.CompileStatic
import org.jclouds.ContextBuilder
import org.jclouds.blobstore.BlobStoreContext

import java.util.Collection

import org.apache.commons.vfs2.Capability
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystemException
import org.apache.commons.vfs2.FileSystemOptions
import org.apache.commons.vfs2.provider.AbstractFileName
import org.apache.commons.vfs2.provider.AbstractFileSystem
import org.ysb33r.groovy.vfsplugin.cloud.core.AbstractCloudFileSystem
import org.jclouds.blobstore.BlobStore

/**
 * An SMB file system.
 */
@CompileStatic
class S3FileSystem extends AbstractCloudFileSystem
{

    protected S3FileSystem(final FileName rootName, final FileSystemOptions fileSystemOptions,ContextBuilder bs) {
        super(rootName, fileSystemOptions,bs)
    }

    /**
     * Creates a file object.
     */
    @Override
    protected FileObject createFile(final AbstractFileName name)  {
        S3FileName fn = name as S3FileName

        BlobStore bs = blobContext.credentials(fn.accessKey,fn.secretKey).buildView(BlobStoreContext.class).blobStore

        return new S3FileObject(name, this, bs)
    }
//
//    /**
//     * Returns the capabilities of this file system.
//     */
//    @Override
//    protected void addCapabilities(Collection<Capability> caps) {
//        Collection<Capability> cc = SmbFileProvider.CAPABILITIES
//        caps.addAll(cc)
//    }
}
