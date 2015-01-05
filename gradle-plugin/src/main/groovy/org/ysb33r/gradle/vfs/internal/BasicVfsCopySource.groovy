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
package org.ysb33r.gradle.vfs.internal

import groovy.transform.CompileStatic
import org.ysb33r.gradle.vfs.VfsCopySource
import org.ysb33r.gradle.vfs.VfsOptions

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
class BasicVfsCopySource implements VfsCopySource {

    private final Object source
//    private final Map<String,Object> options = [:]

    BasicVfsCopySource( Object src ) { source =src }

    /** Source object in non-evaluated object form.
     *
     * @return Object as created by a build script. Never null.
     */
    @Override
    Object getSource() { source }

    /** Options map to be applied to the source when staging or resolving to URI.
     * Can include praxis options..
     *
     * @return Options map. Never null.
     */
    @Override
    VfsOptions getOptions() {
        new VfsOptions() {
            @Override
            Map<String, Object> getOptionMap() { [:] }
        }
    }
}
