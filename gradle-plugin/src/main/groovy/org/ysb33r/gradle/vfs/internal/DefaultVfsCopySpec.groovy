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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.util.CollectionUtils
import org.ysb33r.gradle.vfs.VfsCopySource
import org.ysb33r.gradle.vfs.VfsCopySpec
import org.ysb33r.gradle.vfs.VfsOptions
import org.ysb33r.gradle.vfs.VfsURICollection
import org.ysb33r.groovy.dsl.vfs.VFS

/** Contains a gradlesque collection of sources that will eventually be converted to a
 * a VfsURICollection. Inputs can be strings, File objects, VfsURI instances or closures.
 * It is also used as an aid to delay evaluation of objects into stages or resolved URIs as
 * late as possible.
 *
 * @author Schalk W. Cronjé
 */
@CompileStatic
class DefaultVfsCopySpec implements VfsCopySpec {

    /** Creates an instance of a DefaultVfsCopySpec
     *
     * @param vfs VFS that this copy spec will be associated with
     * @param configurator Initial configurating closure
     * @return The configured DefaultVfsCopySpec instance
     */
    @CompileDynamic
    static DefaultVfsCopySpec create(VFS vfs, Closure configurator) {
        def cs = new DefaultVfsCopySpec(vfs)
        def c = configurator.clone()
        c.delegate = cs
        c.call()
        cs
    }

    /** Applies an additional set of options to each source within the Spec
     * Existing options will NOT be replaced.
     *
     * @param options {@link VfsOptions} to be applied
     * @return This updated copy spec
     */
    @Override
    VfsCopySpec apply(VfsOptions options) {
        this.sources.each { VfsCopySource it ->
            options.optionMap.each { String k,Object v ->
                Map<String,Object> map = it.options.optionMap
                if(!map.hasProperty(k)) {
                    map[k] = v
                }
            }
        }
        this.children.each { it.apply(options) }
        this
    }

    /**
     * Specifies source URIs (files or directories) for a copy. The given paths are evaluated as per
     * {@code VFS.stageURI}. Due to the nature of remote schemes, it is not always possible to determine
     * whether a source is a folder or a file. If scheme does not have a {@code org.apache.commons.vfs2.Capability.LIST_CHILDREN}
     * capability, sources will be assumed to be files.
     * to be directories. I
     *
     * @param sourcePaths Paths to source files for the copy
     */
    @Override
    DefaultVfsCopySpec from(Object... sourcePaths) {
        sourcePaths.each { Object path ->
            this.sources.add (new BasicVfsCopySource(path))
        }
        this
    }

    /**
     * Specifies the source directory for a copy along with a configurating closure. The given source
     * path is evaluated as per {@code VFS.stageURI}.
     *
     * @param sourcePath Path to source for the copy
     * @param configureClosure closure for configuring the child CopySourceSpec
     */
    @Override
    DefaultVfsCopySpec from(Object sourcePath, Closure configureClosure) {
        sources.add(new BasicVfsCopySource(sourcePath,configureClosure))
        this
    }

    /** Sets a relative path to the root
     *
     * @param relativePath relative to a root of a parent copy spec
     * @return This copy spec
     */
    @Override
    VfsCopySpec into(Object relativePath) {
        this.relativePath = relativePath
        this
    }

    /** Returns a collection of staged URIs within this spec, but not child copy specs
     * All sources (supplied via @{code from()} will be evaluated at this time.
     *
     * @return {@link VfsURICollection}. Never null
     */
    @Override
    VfsURICollection getUriCollection() {
        UriUtils.uriWithOptions(vfs,sources)
    }

    /** Returns a collection of staged or resolved URIs of this spec plus all children specs
     * @return {@link VfsURICollection}. Never null.
     */
    @Override
    VfsURICollection getAllUriCollection() {
        VfsURICollection collection = uriCollection
        this.children.each { VfsCopySpec vcs ->
            collection+= vcs.allUriCollection
        }
        collection
    }

    /** Returns the relative resolved from the initial object in a way that is suitable for appending to a URI
     * @return Relative path (can be null)
     */
    @Override
    String getRelativePath() {
        this.relativePath ? CollectionUtils.stringize([this.relativePath])[0] : null
    }

    /** Adds the given specs as children of this spec
     *
     * @param sourceSpecs The specs to add
     * @return This copy spec.
     */
    @Override
    VfsCopySpec with(VfsCopySpec... sourceSpecs) {
        children.addAll(sourceSpecs as List)
        this
    }

    /** Allows for iteration of child specs
     *
     * @return An iterable collection for child specs. Never null.
     */
    @Override
    Iterable children() {
        this.children
    }

    private DefaultVfsCopySpec(VFS vfs) {
        this.vfs = vfs
    }

    private final List<VfsCopySource> sources = []
    private final List<VfsCopySpec> children = []
    private final VFS vfs
    private Object relativePath

}
