package org.ysb33r.rpc
/**
 * Created by schalkc on 02/08/2014.
 */
interface ConnectionCache {
    Connection cache_get(final String server,int port,Protocol proto)
    void cache_put(Connection conn)
}
