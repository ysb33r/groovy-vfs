package org.ysb33r.gradle.vfs.tasks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Incubating
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.ysb33r.gradle.vfs.VfsURI
import org.ysb33r.gradle.vfs.internal.CopyUtils
import org.ysb33r.gradle.vfs.internal.DefaultVfsCopySpec
import org.ysb33r.gradle.vfs.VfsURICollection
import org.ysb33r.gradle.vfs.internal.VfsBaseTask

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

    VfsURICollection getDestinations() {
        stage([destination])
    }

    /** Get a list of all source URIs
     *
     * @return A live list of source URIs
     */
    @Input
    @SkipWhenEmpty
    VfsURICollection getAllSources() {
        getSources()
        // TODO: Needs to traverse all children
    }

    @Input
    VfsURICollection getAllDestinations() {
        stage([destination])
    }

    /** Copies all sources to the destination location.
     *
     */
    @CompileDynamic
    @TaskAction
    def exec() {

        def vfs = super.vfs

        // Process the top-level spec
        def dest = stage(this.destination).resolve()
        CopyUtils.copy(logger,super.vfs,getSources().uris,dest)

//        copySpec.children().each {
//            it.relativePath
//        }
    }

    DefaultVfsCopySpec copySpec = DefaultVfsCopySpec.create(vfs,{})
    Object destination
}
