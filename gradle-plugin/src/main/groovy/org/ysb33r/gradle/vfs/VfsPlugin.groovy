// ============================================================================
// (C) Copyright Schalk W. Cronje 2013
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================

package org.ysb33r.gradle.vfs

import org.gradle.api.*
import org.ysb33r.groovy.dsl.vfs.VFS
import org.apache.commons.logging.LogFactory

class VfsPlugin implements Plugin<Project> {
        
    void apply(Project project) {
        addProjectExtension(project)
    }
    
    void addProjectExtension(Project project) {
        if(!project.metaClass.respondsTo(project,'vfs',Closure)) {
            project.metaClass.__vfs = new VFS ( 
                logger : LogFactory.getLog('vfs'),
                temporaryFileStore : "${project.gradle.gradleUserHomeDir}/vfs"
            )
            project.metaClass.vfs = { Closure c -> project.__vfs.script(c) }
            project.logger.debug 'Added project.vfs'
        }   
    }
}
