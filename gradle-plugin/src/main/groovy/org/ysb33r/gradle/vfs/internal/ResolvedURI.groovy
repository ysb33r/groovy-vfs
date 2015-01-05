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
import org.apache.commons.vfs2.FileObject
import org.gradle.util.CollectionUtils
import org.ysb33r.gradle.vfs.VfsURI
import org.ysb33r.groovy.dsl.vfs.URI
import org.ysb33r.groovy.dsl.vfs.VFS

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
class ResolvedURI implements VfsURI {


    /** Takes a URI and resolves it in the context of a VFS. All supplied options will applied as far as
     * possible. Non-VFS options will be added as praxis options.
     *
     * @param opts Vfs and praxis options
     * @param vfs The VFS to stage against.
     * @param uri The URI to stage
     * @return A resolved URI with praxis options
     */
    static ResolvedURI create( Map<String,Object> opts, VFS vfs, Object uri) {
        Map<String,Object> praxis = opts.findAll { String k, Object v -> !k.startsWith('vfs.') }

        def resolved

        switch(uri) {
            case String:
            case File:
            case FileObject:
            case URI:
                resolved = vfs.resolveURI(opts,uri)
                break

            case Closure:
                resolved = create( opts,vfs,(uri as Closure).call() )
                break

            default:
                resolved= vfs.resolveURI(opts,CollectionUtils.stringize([uri])[0])
        }

        new ResolvedURI( resolved, praxis, vfs.local(resolved) )
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
        true
    }

    /** NOP - This URI is already resolved.
     * @return The resolved URI
     */
    @Override
    VfsURI resolve() {
        this
    }

    private Object uri
    private Map<String,Object> praxis
    private boolean local

    private ResolvedURI( Object uri, Map<String,Object> praxis, boolean local) {
        this.uri = uri
        this.praxis = praxis
        this.local= local
    }

}
