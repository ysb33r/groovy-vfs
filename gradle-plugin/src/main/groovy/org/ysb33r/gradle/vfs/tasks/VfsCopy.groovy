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

package org.ysb33r.gradle.vfs.tasks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Incubating
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.ysb33r.gradle.vfs.VfsOptions
import org.ysb33r.gradle.vfs.VfsURI
import org.ysb33r.gradle.vfs.internal.CopyUtils
import org.ysb33r.gradle.vfs.internal.DefaultVfsCopySpec
import org.ysb33r.gradle.vfs.VfsURICollection
import org.ysb33r.gradle.vfs.internal.ResolvedURI
import org.ysb33r.gradle.vfs.internal.VfsBaseTask
import org.ysb33r.groovy.dsl.vfs.impl.Util

/** Provides a copy task for remote files and directories.
 *
 * Unless collections have been forced to resolve earlier, it will otherwise only happen at the point
 * of task execution. This allows for lazy-evaluation of URIs to occur as late as possible in the build lifecycle.
 *
 * @since 1.0
 * @author Schalk W. Cronjé
 */
@Incubating
@CompileStatic
class VfsCopy extends VfsBaseTask  {

    /** Checks the state of remote objects and decides whether the object can be up to date
     *
     * @return {@code true} if the object can be considered up to date
     */
    @Override
    boolean isUpToDate() {
        return false
    }

    /** Returns a default set of VFS action options (praxis) in case no task-wide set if defined for all URIs
     * within this task.
     *
     * @return Map of options
     */
    @Override
    Map<String, Object> defaultPraxis() {
        [ overwrite : true, recursive : true, smash : false ]
    }

    VfsCopy from(Object... uris) {
        copySpec.from(uris)
        this
    }

    VfsCopy from(Object uri,Closure cfg) {
        copySpec.from(uri,cfg)
        this
    }

    VfsCopy into(Object uri) {
        destination = uri
        this
    }

//    // ---------------------------------
//    // This will be needed, might maybe only in a 2nd update
//    // ---------------------------------
    // VfsCopySpec with(VfsCopySpec childSpecs)

//    // ---------------------------------
//    // Not sure yet, whether these four will be needed
//    // ---------------------------------
//    @InputFiles
//    ResolvedURICollection getLocalSources() {
//        UriUtils.localURIs(stage(sources))
//    }
//
//    @Input
//    ResolvedURICollection getRemoteSources() {
//
//    }
//
//    @org.gradle.api.tasks.Optional
//    @OutputDirectory
//    ResolvedURI getLocalDestination() {
//
//    }
//
//
//    @org.gradle.api.tasks.Optional
//    @Input
//    ResolvedURI getRemoteDestination() {
//
//    }
//
//    // ---------------------------------

    /** Get a list of all source URIs
     *
     * @return A live list of source URIs
     */
    VfsURICollection getSources() {
        stage(copySpec)
    }

    VfsURI getDestination() {
        stage(destination)
    }

    /** Get a list of all source URIs
     *
     * @return A live list of source URIs
     */
    @Input
    @SkipWhenEmpty
    VfsURICollection getAllSources() {
        Map<String,Object> opts = getOptions() + getPraxis()
        copySpec.apply( [getOptionMap : { -> opts } ] as VfsOptions )
        copySpec.allUriCollection
    }

    /** Returns a list of all destination URIs
     * Calling this will cause all URIs to be resolved.
     * @return Fully resolved URIs.
     */
    @Input
    VfsURICollection getAllDestinations() {
        CopyUtils.recursiveDestinationList(super.vfs,copySpec,getDestination())
    }

    /** Copies all sources to the destination location.
     *
     */
    @CompileDynamic
    @TaskAction
    def exec() {
        Map<String,Object> opts = getOptions() + getPraxis()
        copySpec.apply( [getOptionMap : { -> opts } ] as VfsOptions )
        CopyUtils.recursiveCopy(logger,super.vfs,copySpec,getDestination())
    }

    DefaultVfsCopySpec copySpec = DefaultVfsCopySpec.create(vfs,{})
    Object destination
}
