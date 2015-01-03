package org.ysb33r.gradle.vfs.internal

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.Incubating
import org.gradle.api.tasks.Input
import org.ysb33r.gradle.vfs.VfsURI
import org.ysb33r.gradle.vfs.VfsURICollection
import org.ysb33r.groovy.dsl.vfs.VFS

/**
 * Created by schalkc on 01/01/15.
 *
 * @since 1.0
 */
@Incubating
@CompileStatic
abstract class VfsBaseTask extends DefaultTask {

    VfsBaseTask() {
        super()
        outputs.upToDateWhen { VfsBaseTask task ->
            task.isUpToDate()
        }

        this.vfs = VfsFactory.create(super.project)
    }

    /** Checks the state of remote objects and decides whether the object can be up to date
     *
     * @return {@code true} if the object can be considered up to date
     */
    abstract boolean isUpToDate()

    /** Returns a default set of VFS action options (praxis) in case no task-wide set if defined for all URIs
     * within the task. For implementation values see the appropriate operations in the VFS DSL.
     *
     * @return Map of options
     */
    abstract Map<String,Object>  defaultPraxis()

    @Input
    Map<String,Object> getOptions() {
        this.options
    }

//    @CompileDynamic
//    def options(Closure opts) {
//        def exec = opts.clone()
//        exec.delegate = this
//        options += [ '', exec ]
//    }

    def setOptions(Map<String,Object> opts) {
        this.options.clear()
        this.options+= opts
    }

    def options(Map<String,Object> opts) {
        this.options+= opts
    }

    @Input
    Map<String,Object> getPraxis() {
        this.praxis ?: defaultPraxis()
    }

    Map<String,Object> praxis( Map<String,Object> opts) {
        if(this.praxis == null) {
            this.praxis = opts
        } else {
            this.praxis += opts
        }
    }

    Map<String,Object> setPraxis( Map<String,Object> opts) {
        this.praxis = opts
    }


    VFS getVfs() {
        this.vfs
    }

    protected VfsURI stage(Object uri)  {
        UriUtils.uriWithOptions(getOptions(),vfs,uri)
    }

    protected VfsURICollection stage(List<Object> uris)  {
        UriUtils.uriWithOptions(getOptions(),vfs,uris)
    }

    private VFS vfs
    private Map<String,Object> praxis
    private Map<String,Object> options = [:]
}
