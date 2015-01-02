package org.ysb33r.gradle.vfs.tasks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Incubating
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.ysb33r.gradle.vfs.internal.VfsBaseTask

/**
 * Created by schalkc on 01/01/15.
 *
 * @since 1.0
 */
@Incubating
@CompileStatic
class VfsCopy extends VfsBaseTask {

    VfsCopy() {
        super()

        outputs.upToDateWhen { task ->

        }
    }

    def from(Object uri) {

    }

    def into(Object uri) {

    }

    // ---------------------------------
    // Method 1
    // ---------------------------------
    @InputFiles
    def getLocalSources() {

    }

    @Input
    def getRemoteSources() {

    }

    @OutputDirectory
    def getLocalDestination() {

    }

    def getRemoteDestination() {

    }

    // ---------------------------------

    List getSources() {

    }

    def getDestination() {

    }

    @CompileDynamic
    @TaskAction
    def copy() {

        def vfs = super.getVfs()

        def dest = getDestination()
        createDestination(dest)
        def opts = [:]

        vfs {
            sources.each {
                cp it, dest, opts
                logger.info "Copied ${vfs.friendlyURI(it)}"
            }
        }
    }

    @PackageScope
    void createDestination(def dest) {

    }

    List<Object> sources
    Object destination
}
