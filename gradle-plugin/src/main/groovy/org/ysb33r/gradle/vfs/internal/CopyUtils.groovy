package org.ysb33r.gradle.vfs.internal

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.logging.Logger
import org.ysb33r.gradle.vfs.VfsURI
import org.ysb33r.groovy.dsl.vfs.VFS

/** Utilities for VFS copy operations.
 *
 * @author Schalk W. Cronjé
 * @since 1.0
 */
@CompileStatic
class CopyUtils {

    /** Copies a set of sources to a destination, logging success along the way
     *
     * @param logger Logger to report progress
     * @param vfs Virtual file system to use
     * @param sources Source URIs
     * @param root Destination root URI. If it does not exit it will be created prior to starting the copy
     *
     */
    static void copy( Logger logger, VFS vfs, Set<VfsURI> sources, VfsURI root ) {

        vfs {
            if(!exists(root)) {
                mkdir root, intermediates:true
            }

            sources.each { VfsURI src ->
                cp src.praxis, src.uri, root
                logger.info "Copied ${friendlyURI(vfs,src)}"
            }
        }
    }

    @CompileDynamic
    private String friendlyURI(vfs,VfsURI uri) {
        vfs.friendlyURI( uri.uri )
    }
}
