package org.ysb33r.gradle.vfs.internal

import org.apache.commons.logging.LogFactory
import org.gradle.api.Project
import org.ysb33r.groovy.dsl.vfs.VFS

/**
 * Created by schalkc on 01/01/15.
 */
class VfsFactory {
    static VFS create(Project project) {
        new VFS (
            logger : LogFactory.getLog('vfs'),
            temporaryFileStore : "${project.gradle.gradleUserHomeDir}/vfs".toString()
        )
    }
}
