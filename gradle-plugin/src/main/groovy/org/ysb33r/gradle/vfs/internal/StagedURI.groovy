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

import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileObject
import org.gradle.util.CollectionUtils
import org.ysb33r.gradle.vfs.VfsURI
import org.ysb33r.groovy.dsl.vfs.URI
import org.ysb33r.groovy.dsl.vfs.VFS

/** Abstraction to work with unresolved URIs
 *
 * @author Schalk W. Cronjé
 */
class StagedURI implements VfsURI {

    static StagedURI create(Map<String,Object> opts, VFS vfs, Object uri) {

        Object resolved

        switch(uri) {
            case String:
            case File:
            case FileName:
            case URI:
                resolved = uri
                break

            case Closure:
                resolved = (uri as Closure).call()
                break

            default:
                resolved= CollectionUtils.stringize([uri])[0]
        }

        Closure resolver = { Map m, URI u -> ResolvedURI.create(m,vfs,u) }
        Map<String,Object> praxis = opts.findAll { String k,Object v ->
            !k.toLowerCase().startsWith('vfs.')
        }
        URI staged = vfs.stageURI(opts,resolved)
        new StagedURI(praxis,resolver,staged,vfs.local(staged))
    }

    /** URI that has been resolved
     */
    @Override
    Object getUri() {
        this.uri
    }

    /** Any VFS action options (praxis) that are associated with this URI
     *
     * @return List of potential options
     */
    @Override
    Map<String, Object> getPraxis() {
        this.praxis
    }

    /** Whether this URI is local
     *
     * @return {@code True} if local
     */
    @Override
    boolean isLocal() {
        this.local
    }

    /** Returns true is this URI has been resolved
     *
     * @return {@code True} if located on the virtual file system
     */
    @Override
    boolean isResolved() {
        false
    }

    /** Attempts to stage the URI if it is not already resolved,
     *
     * @return A resolved URI
     * @throw Exception if URI cannot be resolved
     */
    @Override
    VfsURI resolve() {
        resolver(praxis,uri)
    }

    URI uri
    Map<String,Object> praxis
    Closure resolver
    boolean local

    private StagedURI( Map<String,Object> praxis, Closure resolver , URI uri, boolean local ) {
        this.uri = uri
        this.praxis = praxis
        this.resolver = resolver
        this.local = local
    }

}
