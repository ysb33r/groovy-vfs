package org.ysb33r.gradle.vfs

import org.apache.commons.logging.LogFactory
import org.gradle.api.Project
import org.ysb33r.groovy.dsl.vfs.VFS

/** Obtains a VFS instance.
 *
 * @author Schalk W. Cronjé
 * @since 1.0
 */
class VfsProxy {
    static final String PROJECT_PROPERTY_NAME = '__vfs'

    static VFS request(Project project) {
        if(project.ext.hasProperty(PROJECT_PROPERTY_NAME)) {
            project.ext."${PROJECT_PROPERTY_NAME}"
        } else {
            new VFS(
                logger: LogFactory.getLog('vfs'),
                temporaryFileStore: "${project.gradle.gradleUserHomeDir}/vfs/${UUID.randomUUID()}".toString()
            )
        }
    }
}
