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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.specs.Spec
import org.ysb33r.gradle.vfs.VfsURI
import org.ysb33r.gradle.vfs.VfsURICollection

/** A live filtered collection over another VfsURICollection
 * @author Schalk W. Cronjé
 */
@CompileStatic
class FilteredVfsURICollection extends AbstractCompositeVfsURICollection {

    static FilteredVfsURICollection create( VfsURICollection collection, Spec<VfsURI> filterSpec) {
        new FilteredVfsURICollection(collection,{ VfsURI it -> filterSpec.isSatisfiedBy(it) } )
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
        String needle = uri.uri.toString()
        collection.any {
            VfsURI u = it as VfsURI
            filterSpec(u) && u.uri.toString() == needle
        }
    }

    /** Check whether filtered collection is empty.
     *
     * @return Returns true if this collection is empty.
     */
    @Override
    boolean isEmpty() {
        !collection.any (filterSpec)
    }

    /** Contents of the collection.
     * This will force all items to be resolved.
     * @return Returns the contents of this collection as a Set.
     */
    @Override
    Set<VfsURI> getUris() {
        resolve()
        collection.findAll (filterSpec) as Set<VfsURI>
    }

    /** Checks whether all URIs in the collection have been resolved.
     *
     * @return {@code True} is all are resolved.
     */
    @Override
    boolean allResolved() {
        !(collection.findAll (filterSpec). any { !(it as VfsURI).resolved })
    }

    /** Attempt to have all URIs resolved. Throws if there is a failure.
     *
     * @return This collection with all URIs resolved
     * @throw StopExecutionException if a URI cannot be resolved.
     */
    @Override
    VfsURICollection resolve() {
        collection.resolve()
        this
    }

    /** Restricts the contents of this collection to those files which match the given criteria.
     * The filtered collection is live, so that it reflects any changes to this collection.
     *
     * @param filterSpec The criteria to use to select the contents of the filtered collection.
     * @return The filtered collection.
     */
    @Override
    @CompileDynamic
    VfsURICollection filter(Spec<VfsURI> nextFilterSpec) {
        def newFilter = filterSpec.clone()
        Spec<VfsURI> nextFilter = [
            isSatisfiedBy : { VfsURI element ->
                 newFilter(element) && nextFilterSpec.isSatisfiedBy(element)
            }
        ] as Spec<VfsURI>
        create(collection,nextFilter)
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    Iterator iterator() {
        collection.findAll (filterSpec).iterator()
    }

    private FilteredVfsURICollection( VfsURICollection watchedCollection, Closure filterSpec ) {
        this.collection = watchedCollection
        this.filterSpec = filterSpec
    }

    private final VfsURICollection collection
    private final Closure filterSpec
}
