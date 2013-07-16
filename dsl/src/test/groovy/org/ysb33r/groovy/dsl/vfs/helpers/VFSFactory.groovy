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

package org.ysb33r.groovy.dsl.vfs.helpers

import org.apache.commons.logging.impl.SimpleLog
import org.ysb33r.groovy.dsl.vfs.VFS

/** Creates VFS instances used for testing schemas
 * 
 * @author schalkc
 *
 */
class VFSFactory {
    
    static def createInstance(testName) {
        def simpleLog = new SimpleLog(testName)
        simpleLog.setLevel( SimpleLog.LOG_LEVEL_ALL )
        new VFS( logger: simpleLog )
    }
}

