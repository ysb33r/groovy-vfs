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

package org.ysb33r.gradle.vfs.tasks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Incubating
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.ysb33r.gradle.vfs.VfsCopySpec
import org.ysb33r.gradle.vfs.VfsOptions
import org.ysb33r.gradle.vfs.VfsURI
import org.ysb33r.gradle.vfs.internal.CopyUtils
import org.ysb33r.gradle.vfs.internal.DefaultVfsCopySpec
import org.ysb33r.gradle.vfs.VfsURICollection
import org.ysb33r.gradle.vfs.internal.UpToDateCheck
import org.ysb33r.gradle.vfs.internal.VfsBaseTask
import org.ysb33r.groovy.dsl.vfs.VFS

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

    /** Certain remote systems do not report modified dates correctly, resulting in unnecessary download.
     * If this is the case date checks can be turned off for as task instance. This will result in files only being
     * downloaded if the target does not exists or {@code --rerun-tasks} is passed.
     */
    @Input
    boolean noSourceModifiedDateCheck = false


    /** Checks the state of remote objects and decides whether the object can be up to date.
     * Up to date can be considered for the following considerations
     * <ul>
     *   <li>Source and destination filesystems all support {@code Capability>GET_MODIFIED_DATE} and all destination
     *     files are of same or newer date than their source files
     *   </li>No files on the source is missing from the destination
     * </ul>
     *
     * @return {@code true} if the object can be considered up to date
     */
    @Override
    boolean isUpToDate() {
        Map<String,Object> opts = getOptions() + getPraxis()
        copySpec.apply( [getOptionMap : { -> opts } ] as VfsOptions )
        return UpToDateCheck.forCopySpec(logger,super.vfs,copySpec,getDestination(),noSourceModifiedDateCheck)
    }

    /** Returns a default set of VFS action options (praxis) in case no task-wide set if defined for all URIs
     * within this task.
     *
     * @return Map of options
     */
    @Override
    Map<String, Object> defaultPraxis() {
        [ overwrite : VFS.onlyNewer, recursive : true, smash : false ]
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

    VfsCopy with(VfsCopySpec... copySpecs) {
        copySpec.with(copySpecs)
        this
    }

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
    VfsURICollection getAllSources() {
        Map<String,Object> opts = getOptions() + getPraxis()
        copySpec.apply( [getOptionMap : { -> opts } ] as VfsOptions )
        copySpec.allUriCollection
    }

    /** Get a list of all source URIs converted to String representations
     *
     * @return
     */
    @Input
    Set<String> getAllSourceURIs() {
        allSources.uris.collect {
            it.uri.toString()
        } as Set
    }

    /** Returns a list of all destination URIs
     * Calling this will cause all URIs to be resolved.
     * @return Fully resolved URIs.
     */
    VfsURICollection getAllDestinations() {
        CopyUtils.recursiveDestinationList(super.vfs,copySpec,getDestination())
    }

    /** Returns a list of all destination URIs as strings
     * Calling this will cause all URIs to be resolved.
     * @return Fully resolved URIs.
     */
    @Input
    Set<String> getAllDestinationURIs() {
        allDestinations.uris.collect { it.uri.toString() } as Set
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
