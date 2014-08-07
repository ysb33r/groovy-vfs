package org.ysb33r.rpc.cache

import groovy.transform.CompileStatic
import org.ysb33r.rpc.Connection
import org.ysb33r.rpc.ConnectionCache
import org.ysb33r.rpc.Protocol

/**
 * Created by schalkc on 03/08/2014.
 */
@CompileStatic
class ConnectionBasicCache implements ConnectionCache {
    /**
     * Get a cached connection for the specified server, port and ip
     *
     * @param server	The hostname of the server
     * @param port	The port number on the server
     * @param proto	The connection type: "tcp" or "udp"
     * @returns null	If there is no cached connection
     */
    @Override
    Connection cache_get(final String server, int port, final Protocol proto) {
        connections[key(server,port,proto)] as Connection
    }

    /**
     * Stash a new connection in the cache
     *
     * @param	The connection to be cached
     */
    @Override
    void cache_put(Connection conn) {
        connections[key(conn.server,conn.port,conn.proto)] = conn
    }

    private String key(final String server,int port,Protocol proto) {
        "${server}:${port}:${proto.name}".toString()
    }

    private Map<String,Connection> connections = [:]
}

