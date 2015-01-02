package org.ysb33r.gradle.vfs.internal

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Incubating
import org.gradle.api.tasks.Input
import org.ysb33r.groovy.dsl.vfs.VFS

/**
 * Created by schalkc on 01/01/15.
 *
 * @since 1.0
 */
@Incubating
@CompileStatic
class VfsBaseTask extends DefaultTask {

    VfsBaseTask() {
        super()
        this.vfs = VfsFactory.create(super.project)
    }

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
        this.praxis
    }

    Map<String,Object> praxis( Map<String,Object> opts) {
        this.praxis += opts
    }

    Map<String,Object> setPraxis( Map<String,Object> opts) {
        this.praxis = opts
    }

    boolean isUpToDate() {
        throw new GradleException('isUpToDate() is not implemented')
    }

    VFS getVfs() {
        this.vfs
    }

    protected def resolve(Object uri)  {
        vfs.resolveURI(getOptions(),uri)
    }

    protected def resolve(List<Object> uris)  {
        def opts = getOptions()
        uris.collect {
            vfs.resolveURI(opts,it)
        }
    }

    private VFS vfs
    private Map<String,Object> praxis = [:]
    private Map<String,Object> options = [:]
}
