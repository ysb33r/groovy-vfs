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
import org.gradle.api.tasks.StopExecutionException
import org.ysb33r.gradle.vfs.VfsURI
import org.ysb33r.gradle.vfs.VfsURICollection

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
abstract class AbstractCompositeVfsURICollection implements VfsURICollection {

    /** Adds a URI to this collection
     *
     * @param uri URI to be added
     * @return This updated collection
     * @throw Throws UnsupportedOperationException if URIs addition is not supported
     */
    @Override
    VfsURICollection add(VfsURI uri) {
        throw new UnsupportedOperationException('Cannot add to a filtered or composite URI collection')
    }

    /** Throws a StopExecutionException if this collection is empty.
     *
     * @return Return this collection if it is not empty
     */
    @Override
    VfsURICollection stopExecutionIfEmpty() {
        if(isEmpty()) {
            throw new StopExecutionException('Vfs URI collection is empty')
        }
        this
    }

    /** Restricts the contents of this collection to those URIs which match the given criteria.
     * The filtered collection is live, so that it reflects any changes to this collection.
     * The given closure is passed the VfsURI as a parameter, and should return a boolean value.
     * @param filterClosure The closure to use to select the contents of the filtered collection
     * @return The filtered collection
     */
    @Override
    VfsURICollection filter(Closure filterClosure) {
        Spec<VfsURI> filterSpec = [ isSatisfiedBy : filterClosure ] as Spec<VfsURI>
        this.filter(filterSpec)
    }

    abstract VfsURICollection filter(Spec<VfsURI> filterSpec)

    /** Creates a new live collection out of two collections
     *
     * @param collection
     * @return Sum of two collections
     */
    @Override
    VfsURICollection plus(VfsURICollection collection) {
        UnionVfsURICollection.create(this,collection)
    }

    /** Returns a new collection which is the difference between two collections.
     *
     * @param collection
     * @return Difference between the two collections
     */
    @Override
    VfsURICollection minus(VfsURICollection collection) {
        ReducedVfsURICollection.create(this,collection)
    }
}
