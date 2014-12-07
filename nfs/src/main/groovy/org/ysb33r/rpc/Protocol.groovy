package org.ysb33r.rpc

import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * Created by schalkc on 03/08/2014.
 */
@CompileStatic
@ToString
enum Protocol {
    TCP('tcp'),
    UDP('udp')

    final String name
    Protocol(final String n) {name=n}
}
