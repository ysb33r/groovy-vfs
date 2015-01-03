package org.ysb33r.gradle.vfs.tasks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Incubating
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.ysb33r.gradle.vfs.VfsURICollection
import org.ysb33r.gradle.vfs.internal.VfsBaseTask

/**
 *
 * @author Schalk W. Cronjé
 * @since 1.0
 */
@Incubating
@CompileStatic
class VfsMkdir extends VfsBaseTask {

    /** Alias for {@code directories}
     *
     * @param dirs List of directories
     */
    void dirs(Object... dirs) {directories(dirs)}

    void directories(Object... dirs)  {
        this.directories += (dirs as List)
    }

    void setDirectories(Object... dirs) {
        this.directories.clear()
        this.directories.addAll(dirs as List)
    }

    /** Stages all URIs and return a collection of directories to be created.
     * @return Collection of resolved URIs
     */
    @Input
    @SkipWhenEmpty
    VfsURICollection getDirectories() {
        stage(this.directories)
    }

    /** Check whether this task should be considered up to date.
     * As this means checking on remote servers for existance of directories, it will requires all directory URIs to be
     * resolved and as such also some potential network traffic.
     *
     * @return {@code True} if up to date.
     */
    @Override
    boolean isUpToDate() {
        getDirectories().resolve().uris.every { vfs.exists(it.uri) }
    }

    /* If praxis is not set then the set {@code intermediates = true}
    *
     */
    @Override
    Map<String, Object> defaultPraxis() {
        [ intermediates : true ]
    }

    @CompileDynamic
    @TaskAction
    void exec() {
        def opts = getPraxis()
        def vfs = super.vfs

        vfs {
            getDirectories().uris.each {
                if(!exists(it.uri)) {
                    mkdir opts, it.uri
                    logger.info "Created ${friendlyURI(it.uri)}"
                }
            }
        }
    }

    private List<Object> directories = []
}
