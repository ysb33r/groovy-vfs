package org.ysb33r.gradle.vfs.internal

import org.ysb33r.gradle.vfs.VfsOptions

/**
 * @author Schalk W. Cronjé
 */
class Configurator implements VfsOptions {

    static Configurator execute( Closure cfg ) {
        def config= new Configurator()
        def c = cfg.clone()
        c.delegate=config
        c.call()
        config
    }

    Map<String,Object> getOptionMap() {
        // TODO: IMPLEMENT
        [:]
    }

    def methodMissing(String name,Object... args) {

    }
}
