package org.ysb33r.rpc

import groovy.transform.CompileStatic

/** RPC reject status
 * @author Schalk W. Cronjé
 */
@CompileStatic
enum RejectStatus {
    RPC_MISMATCH (0,"Version mismatch"),
    AUTH_ERROR   (1,"Authentication error")

    final int raw
    final String description

    static RejectStatus fromInt(int i) {
        switch (i) {
            case 0:
                return RPC_MISMATCH
            case 1:
                return AUTH_ERROR
            default:
                throw new IllegalArgumentException("${i} is not a legal RPC Reject Status")
        }
    }
    private RejectStatus(int value,final String d) {
        raw = value
        description = d
    }

}
