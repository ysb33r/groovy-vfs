package org.ysb33r.nfs

/**
 * Created by schalkc on 02/08/2014.
 */
interface NfsCache {
    Nfs cache_get(final String server,final String path)
    void cache_put(Nfs n)
    void cache_remove(Nfs n,final String name)
}
