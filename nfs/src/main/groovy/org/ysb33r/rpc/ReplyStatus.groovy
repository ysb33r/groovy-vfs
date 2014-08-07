package org.ysb33r.rpc

import groovy.transform.CompileStatic

/** RPC reply status
 * @author Schalk W. Cronjé
 */
@CompileStatic
enum ReplyStatus {
    MSG_ACCEPTED(0),
    MSG_DENIED(1)

    final int raw

    static ReplyStatus fromInt(int i) {
        switch (i) {
            case 0:
                return MSG_ACCEPTED
            case 1:
                return MSG_DENIED
            default:
                throw new IllegalArgumentException("${i} is not a legal RPC Reply Status")
        }
    }
    private ReplyStatus(int value) {raw = value}

}
