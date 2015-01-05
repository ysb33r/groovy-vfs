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

import org.gradle.api.Incubating
import org.gradle.api.specs.Spec

/** Similar to a {@code org.gradle.api.file.FileCollection}, this manages a collection of
 * URIs.
 *
 * @since 1.0
 */
@Incubating
interface VfsURICollection extends Iterable {
    /** Adds a URI to this collection
     *
     * @param uri URI to be added
     * @return This updated collection
     * @throw Throws UnsupportedOperationException if URIs addition is not supported
     */
    VfsURICollection add( VfsURI uri )

    /** Checks whether a specific URI is within a collection. The strings representations
     * are compared to determine whether presence. This allows for both resolved and taged URIs
     * to be compared. ANy vfs query properties will be ignored.
     *
     * @param uri
     * @return Return {@code true} is the uri is contained within the collection.
     */
    boolean contains(VfsURI uri)

    /** Check whether collection is empty
     *
     * @return Returns true if this collection is empty.
     */
    boolean 	isEmpty()

    /** Contents of the collection.
     * This will force all items to be resolved.
     * @return Returns the contents of this collection as a Set.
     */
    Set<VfsURI> getUris()

    /** Restricts the contents of this collection to those URIs which match the given criteria.
     * The filtered collection is live, so that it reflects any changes to this collection.
     * The given closure is passed the VfsURI as a parameter, and should return a boolean value.
     * @param filterClosure The closure to use to select the contents of the filtered collection
     * @return The filtered collection
     */
    VfsURICollection 	filter(Closure filterClosure)

    /** Restricts the contents of this collection to those files which match the given criteria.
     * The filtered collection is live, so that it reflects any changes to this collection.
     *
     * @param filterSpec The criteria to use to select the contents of the filtered collection.
     * @return The filtered collection.
     */
    VfsURICollection 	filter(Spec<VfsURI> filterSpec)

    /** Returns a new collection which is the difference between two collections.
     *
     * @param collection
     * @return Difference between the two collections
     */
    VfsURICollection minus(VfsURICollection collection)

    /** Creates a new collection out of two collections
     *
     * @param collection
     * @return SUm of two collections
     */
    VfsURICollection plus(VfsURICollection collection)

    /** Throws a StopExecutionException if this collection is empty.
     *
     * @return Return this collection if it is not empty
     */
    VfsURICollection stopExecutionIfEmpty()

    /** Checks whether all URIs in the collection have been resolved.
     *
     * @return {@code True} is all are resolved.
     */
    boolean allResolved()

    /** Attempt to have all URIs resolved. Throws if there is a failure.
     *
     * @return This collection with all URIs resolved
     * @throw StopExecutionException if a URI cannot be resolved.
     */
    VfsURICollection resolve()


}