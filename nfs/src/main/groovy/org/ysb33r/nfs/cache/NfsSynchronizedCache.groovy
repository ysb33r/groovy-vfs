package org.ysb33r.nfs.cache

import groovy.transform.CompileStatic
import groovy.transform.Synchronized
import org.ysb33r.nfs.Nfs
import org.ysb33r.nfs.NfsCache
import org.ysb33r.nfs.cache.NfsBasicCache

/** A synchronized version of NFS Cache
 * @author Schalk W. Cronjé
 */
@CompileStatic
class NfsSynchronizedCache implements NfsCache {

    /**
     * Retrieve a cached Nfs object
     *
     * @param server	The server that hosts the object
     * @param name	The pathname of the object
     * @returns		The object - or null if not cached
     */
    @Synchronized
    @Override
    Nfs cache_get(final String server,final String path) {
        cache.cache_get(server,path)
    }

    /**
     * Cache an Nfs object
     *
     * @param n	the object to be cached
     */
    @Synchronized
    @Override
    void cache_put(Nfs n) {
        cache.cache_put(n)
    }

    /**
     * Remove an Nfs object from the cache
     *
     * @param n	the object to be removed from cache
     */
    @Synchronized
    @Override
    void cache_remove(Nfs n, String name) {
        cache.cache_remove(n,name)
    }

    private NfsBasicCache cache = new NfsBasicCache()
}
