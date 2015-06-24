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

package org.ysb33r.gradle.vfs

import org.apache.commons.logging.LogFactory
import org.gradle.api.Project
import org.ysb33r.groovy.dsl.vfs.VFS

/** Obtains a VFS instance.
 *
 * @author Schalk W. Cronjé
 * @since 1.0
 */
class VfsProxy {
    static final String PROJECT_PROPERTY_NAME = '__vfs'

    static VFS request(Project project) {
        if(project.ext.hasProperty(PROJECT_PROPERTY_NAME)) {
            project.ext."${PROJECT_PROPERTY_NAME}"
        } else {
            new VFS(
                logger: LogFactory.getLog('vfs'),
                temporaryFileStore: "${project.gradle.gradleUserHomeDir}/vfs/${UUID.randomUUID()}".toString()
            )
        }
    }
}
