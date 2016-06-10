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

package org.ysb33r.groovy.vfsplugin.cloud.s3

import groovy.transform.CompileStatic
import org.jclouds.ContextBuilder
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileSystemOptions
import org.ysb33r.groovy.vfsplugin.cloud.core.AbstractCloudFileProvider
import org.ysb33r.groovy.vfsplugin.cloud.core.AbstractCloudFileSystem

/**
 * A provider for SMB (Samba, Windows share) file systems.
 */
@CompileStatic
class S3FileProvider extends AbstractCloudFileProvider
{
//    final static Collection<Capability> CAPABILITIES = [
//        CREATE,
//        DELETE,
//        RENAME,
//        GET_TYPE,
//        GET_LAST_MODIFIED,
//        SET_LAST_MODIFIED_FILE,
//        SET_LAST_MODIFIED_FOLDER,
//        LIST_CHILDREN,
//        READ_CONTENT,
//        Capability.URI,
//        WRITE_CONTENT,
//        APPEND_CONTENT,
//        RANDOM_ACCESS_READ,
//        RANDOM_ACCESS_WRITE
//    ]

    S3FileProvider() {
        super(S3FileNameParser.instance)
    }

    @Override
    protected String getCloudProvider() { return "aws-s3" }

    /**
     * Creates the filesystem.
     */
    @Override
    protected AbstractCloudFileSystem doCreateFileSystem(
            final FileName name, final FileSystemOptions fsOpt, ContextBuilder bs) {
        return new S3FileSystem(name,fsOpt,bs)
    }

//    @Override
//    Collection<Capability> getCapabilities() { CAPABILITIES }
}
