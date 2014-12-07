package org.ysb33r.nfs

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TupleConstructor
import org.ysb33r.rpc.Protocol

/**
 * Created by schalkc on 02/08/2014.
 */
@TupleConstructor
@ToString
@CompileStatic
class NfsConnection {
    String server
    int port
    int version
    Protocol proto
    boolean pub
    String sec_flavour = null
}
