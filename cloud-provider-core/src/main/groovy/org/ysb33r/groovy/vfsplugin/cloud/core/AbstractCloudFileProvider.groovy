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

import groovy.transform.CompileStatic
import groovy.transform.CompileDynamic
import org.apache.commons.vfs2.Capability
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileSystem
import org.apache.commons.vfs2.FileSystemOptions
import org.apache.commons.vfs2.provider.FileNameParser
import org.apache.commons.vfs2.provider.FileProvider
import org.apache.commons.vfs2.util.UserAuthenticatorUtils
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider
import org.apache.commons.vfs2.UserAuthenticationData
import static org.apache.commons.vfs2.Capability.*
import static org.apache.commons.vfs2.UserAuthenticationData.*
import org.jclouds.ContextBuilder
import org.jclouds.blobstore.BlobStoreContext
import org.jclouds.blobstore.BlobStore
import org.jclouds.ContextBuilder
import org.ysb33r.groovy.vfsplugin.cloud.core.AbstractCloudFileSystem

/**
 * A provider for SMB (Samba, Windows share) file systems.
 */
@CompileStatic
abstract class AbstractCloudFileProvider extends AbstractOriginatingFileProvider
{
    final static UserAuthenticationData.Type[]  AUTHENTICATOR_TYPES = [
            USERNAME, PASSWORD
    ] as UserAuthenticationData.Type[]

    final static Collection<Capability> CAPABILITIES = [
            CREATE,
            DELETE,
            GET_TYPE,
            GET_LAST_MODIFIED,
            LAST_MODIFIED,
//            SET_LAST_MODIFIED_FILE,
//            SET_LAST_MODIFIED_FOLDER,
            LIST_CHILDREN,
            READ_CONTENT,
            Capability.URI,
            WRITE_CONTENT
    ]

   AbstractCloudFileProvider( final FileNameParser parser) {
        super()
        fileNameParser= parser
    }

    protected abstract String getCloudProvider()
    protected abstract AbstractCloudFileSystem doCreateFileSystem(
            final FileName name, final FileSystemOptions filesystemOptions, ContextBuilder bs
    )

    /**
     * Creates the filesystem.
     */
    @Override
    protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fsOpt) {

// TODO: Enable logging inside jClouds        logger.info("Initialize Amazon S3 service client ...");

        ContextBuilder cb = setCredentials(ContextBuilder.newBuilder(cloudProvider),fsOpt)
//                   context.modules( ImmutableSet.<Module> of(new SLF4JLoggingModule()) )
//                buildView(BlobStoreContext.class).blobStore

        doCreateFileSystem(name,fsOpt,cb)
    }

    protected ContextBuilder setCredentials(ContextBuilder context, final FileSystemOptions filesystemOptions) {
        UserAuthenticationData authData = UserAuthenticatorUtils.authenticate(filesystemOptions, AUTHENTICATOR_TYPES)
        if(authData) {
            final String username = UserAuthenticatorUtils.toString(UserAuthenticatorUtils.getData(authData, USERNAME, null))
            final String password = UserAuthenticatorUtils.toString(UserAuthenticatorUtils.getData(authData, PASSWORD, null))
            context.credentials(username, password)
        } else {
            context
        }
    }


    @Override
    Collection<Capability> getCapabilities() { CAPABILITIES }
}
