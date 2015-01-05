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

import org.apache.commons.vfs2.FileSystemConfigBuilder

/**
 * Created by schalkc on 08/05/2014.
 */
abstract class AbstractCloudFileSystemConfigBuilder extends FileSystemConfigBuilder {
    protected AbstractCloudFileSystemConfigBuilder(final String prefix) { super(prefix) }
}
