package org.ysb33r.gradle.vfs.tasks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Incubating
import org.gradle.api.Task
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.ysb33r.gradle.vfs.internal.VfsBaseTask

/**
 * Created by schalkc on 01/01/15.
 *
 * @since 1.0
 */
@Incubating
@CompileStatic
class VfsMkdir extends VfsBaseTask {

    VfsMkdir() {
        super()

        outputs.upToDateWhen { VfsMkdir task ->
            task.isUpToDate()
        }
    }

    void directories(Object... dirs)  {
        this.directories += (dirs as List)
    }

    void setDirectories(Object... dirs) {
        this.directories.clear()
        this.directories.addAll(dirs as List)
    }

    @Input
    def getDirectories() {
        resolve(this.directories)
    }

    @Override
    boolean isUpToDate() {
        getDirectories().every { vfs.exists(it) }
    }

    @CompileDynamic
    @TaskAction
    void exec() {
        def opts = getPraxis()
        def vfs = super.vfs

        vfs {
            getDirectories().each {
                if(!exists(it)) {
                    mkdir opts, it
                }
            }
        }
    }

    private List<Object> directories = []
}
