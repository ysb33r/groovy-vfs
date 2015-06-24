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

package org.ysb33r.gradle.vfs.internal

import groovy.transform.CompileStatic
import org.gradle.api.specs.Spec
import org.ysb33r.gradle.vfs.VfsURI
import org.ysb33r.gradle.vfs.VfsURICollection

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
class UnionVfsURICollection extends AbstractCompositeVfsURICollection {

    static UnionVfsURICollection create( VfsURICollection primary, VfsURICollection secondary ) {
        new UnionVfsURICollection(primary,secondary)
    }

    /** Checks whether a specific URI is within a collection. The strings representations
     * are compared to determine whether presence. This allows for both resolved and taged URIs
     * to be compared. ANy vfs query properties will be ignored.
     *
     * @param uri
     * @return Return {@code true} is the uri is contained within the collection.
     */
    @Override
    boolean contains(VfsURI uri) {
        primaryCollection.contains(uri) || secondaryCollection.contains(uri)
    }

    /** Check whether collection is empty
     *
     * @return Returns true if this collection is empty.
     */
    @Override
    boolean isEmpty() {
        primaryCollection.isEmpty() && secondaryCollection.isEmpty()
    }

    /** Contents of the collection.
     * This will force all items to be resolved.
     * @return Returns the contents of this collection as a Set.
     */
    @Override
    Set<VfsURI> getUris() {
        (primaryCollection.uris + secondaryCollection.uris) as Set<VfsURI>
    }

    /** Restricts the contents of this collection to those files which match the given criteria.
     * The filtered collection is live, so that it reflects any changes to this collection.
     *
     * @param filterSpec The criteria to use to select the contents of the filtered collection.
     * @return The filtered collection.
     */
    @Override
    VfsURICollection filter(Spec<VfsURI> filterSpec) {
        FilteredVfsURICollection.create(this,filterSpec)
    }

    /** Checks whether all URIs in the collections have been resolved.
     *
     * @return {@code True} is all are resolved.
     */
    @Override
    boolean allResolved() {
        primaryCollection.allResolved() && secondaryCollection.allResolved()
    }

    /** Attempt to have all URIs in both collections resolved. Throws if there is a failure.
     *
     * @return This collection with all URIs resolved
     * @throw StopExecutionException if a URI cannot be resolved.
     */
    @Override
    VfsURICollection resolve() {
        primaryCollection.resolve()
        secondaryCollection.resolve()
        this
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    Iterator iterator() {
        [primaryCollection,secondaryCollection].flatten().iterator()
    }

    private UnionVfsURICollection(VfsURICollection prim,VfsURICollection sec) {
        primaryCollection = prim
        secondaryCollection = sec
    }

    private final VfsURICollection primaryCollection
    private final VfsURICollection secondaryCollection
}
