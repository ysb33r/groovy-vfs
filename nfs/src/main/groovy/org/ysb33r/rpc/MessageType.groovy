package org.ysb33r.rpc

import groovy.transform.CompileStatic

/** RPC message type
 * @author Schalk W. Cronjé
 */
@CompileStatic
enum MessageType {
    CALL(0),
    REPLY(1)

    final int raw

    static MessageType fromInt(int i) {
        switch (i) {
            case 0:
                return CALL
            case 1:
                return REPLY
            default:
                throw new IllegalArgumentException("${i} is not a legal RPC Message Type")
        }
    }
    private MessageType(int value) {raw = value}

}
