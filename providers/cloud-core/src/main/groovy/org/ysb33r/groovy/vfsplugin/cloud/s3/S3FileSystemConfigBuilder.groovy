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

// TODO: Add specific S3 config
// TODO: Add ACL operations