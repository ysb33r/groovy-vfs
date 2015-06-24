/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2015
 *
 * This software is licensed under the Apache License 2.0
 * See http://www.apache.org/licenses/LICENSE-2.0 for license details
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 * ============================================================================
 */
//
// ============================================================================
// (C) Copyright Schalk W. Cronje 2013-2015
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================
//
package org.ysb33r.groovy.vfsplugin.cloud.core

import org.apache.commons.vfs2.Capability
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystemException
import org.apache.commons.vfs2.FileSystemOptions
import org.apache.commons.vfs2.provider.AbstractFileName
import org.apache.commons.vfs2.provider.AbstractFileSystem
import org.jclouds.ContextBuilder
import org.jclouds.blobstore.BlobStore

/**
 * Created by schalkc on 07/05/2014.
 */
abstract class AbstractCloudFileSystem extends AbstractFileSystem {

    protected ContextBuilder blobContext

    protected AbstractCloudFileSystem(
            final FileName rootName, final FileSystemOptions fsOpt, ContextBuilder bs) {
        super(rootName, null, fsOpt)
        blobContext = bs
    }

//    /**
//     * Creates a file object.
//     */
//    @Override
//    protected FileObject createFile(final AbstractFileName name) throws FileSystemException {
//        return new SmbFileObject(name, this)
//    }

    /**
     * Returns the capabilities of this file system.
     */
    @Override
    protected void addCapabilities(Collection<Capability> caps) {
        caps.addAll(AbstractCloudFileProvider.CAPABILITIES)
    }
}
