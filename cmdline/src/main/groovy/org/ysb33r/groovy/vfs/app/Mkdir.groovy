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
package org.ysb33r.groovy.vfs.app

import groovy.transform.TupleConstructor
import org.ysb33r.groovy.dsl.vfs.VFS

//        -p, --parents
//        no error if existing, make parent directories as needed

@TupleConstructor
class Mkdir implements Cmd {

    boolean intermediates = false
    Integer mode = null
    List<org.ysb33r.groovy.dsl.vfs.URI> uris = []

    boolean isInteractive() { false }

    Integer run(VFS vfs) {
        vfs {
            uris.each {
                mkdir it, 'intermediates' : intermediates
            }
        }
        return 0i
    }
}