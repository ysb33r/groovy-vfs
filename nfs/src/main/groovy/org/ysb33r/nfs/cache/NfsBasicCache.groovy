package org.ysb33r.nfs.cache

import groovy.transform.CompileStatic
import org.ysb33r.nfs.Nfs
import org.ysb33r.nfs.NfsCache

/**
 * Created by schalkc on 03/08/2014.
 */
@CompileStatic
class NfsBasicCache implements NfsCache {

    /**
     * Retrieve a cached Nfs object
     *
     * @param server	The server that hosts the object
     * @param name	The pathname of the object
     * @returns		The object - or null if not cached
     */
    @Override
    Nfs cache_get(final String server,final String path) {
        cache[key(server,path)]
    }

    /**
     * Cache an Nfs object
     *
     * @param n	the object to be cached
     */
    @Override
    void cache_put(Nfs n) {
        cache[key(n.rpc().conn.server,n.fName())] = n
    }

    /**
     * Remove an Nfs object from the cache
     *
     * @param n	the object to be removed from cache
     */
    @Override
    void cache_remove(Nfs n, String name) {
        if (n.fName().equals("."))
            cache.remove( key(n.rpc().conn.server,name) )
        else
            cache.remove( key(n.rpc().conn.server,n.fName(),name) )
    }

    private String key(final String server,final String path) {
        "${server}:${path}".toString()
    }

    private String key(final String server,final String name, final String path) {
        "${server}:${name}/${path}".toString()
    }

    private Map<String,Nfs> cache = [:]
}
