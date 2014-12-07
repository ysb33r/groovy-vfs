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
import org.apache.commons.vfs2.FileNotFolderException
import org.apache.commons.vfs2.FileSystemException
import org.jclouds.blobstore.BlobStore
import org.jclouds.blobstore.domain.BlobMetadata
import org.jclouds.blobstore.domain.StorageMetadata
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileType
import org.apache.commons.vfs2.provider.AbstractFileName
import org.apache.commons.vfs2.provider.UriParser
import org.jclouds.blobstore.options.ListContainerOptions
import org.ysb33r.groovy.vfsplugin.cloud.core.AbstractCloudFileObject

/**
 * A file in an SMB file system.
 */
@CompileStatic
class S3FileObject extends AbstractCloudFileObject {

    protected S3FileObject(final AbstractFileName name, final S3FileSystem fileSystem,BlobStore bs) {
        super(name, fileSystem,bs)
        // this.fileName = UriParser.decode(name.getURI());
    }

    @Override
    protected String getCloudContainerName() {
        S3FileName s3name = this.name as S3FileName
        s3name.bucket
    }

}
