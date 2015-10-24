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

/** Abstraction to work with resolved VFS URIs.
 *
 * @author Schalk W. Cronjé
 */
interface VfsURI{

    /** URI that has been resolved
     */
    Object getUri()

    /** Any VFS action options (praxis) that are associated with this URI
     *
     * @return List of potential options
     */
    Map<String,Object> getPraxis()

    /** Whether this URI is local
     *
     * @return {@code True} if local
     */
    boolean isLocal()

    /** Returns true is this URI has been resolved
     *
     * @return {@code True} if located on the virtual file system
     */
    boolean isResolved()

    /** Attempts to stage the URI if it is not already resolved,
     *
     * @return A resolved URI
     * @throw Exception if URI cannot be resolved
     */
    VfsURI resolve()
}