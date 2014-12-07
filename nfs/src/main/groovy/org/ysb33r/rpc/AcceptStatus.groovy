package org.ysb33r.rpc

import groovy.transform.CompileStatic

/** RPC accept status
 * @author Schalk W. Cronjé
 */
@CompileStatic
enum AcceptStatus {

    SUCCESS        (0,"Success"),
    PROG_UNAVAIL   (1,"Program unavailable"),
    PROG_MISMATCH  (2,"Program number mismatch"),
    PROC_UNAVAIL   (3,"Procedure unavailable"),
    GARBAGE_ARGS   (4,"Garbage Arguments"),
    SYSTEM_ERR     (5,"System error"),
    UNSPECIFIED    (-1,"Unspecified accept status")


    final int raw
    final String description

    static AcceptStatus fromInt(int i) {
        switch (i) {
            case 0:
                return SUCCESS
            case 1:
                return PROG_UNAVAIL
            case 2:
                return PROG_MISMATCH
            case 3:
                return PROC_UNAVAIL
            case 4:
                return GARBAGE_ARGS
            case 5:
                return SYSTEM_ERR
            default:
                return UNSPECIFIED
        }
    }

    private AcceptStatus(int value,final String d) {
        raw = value
        description= d
    }

}
