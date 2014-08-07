package org.ysb33r.rpc

import groovy.transform.CompileStatic

/** RPC authentication failure type
 * @author Schalk W. Cronjé
 */
@CompileStatic
enum AuthFailureType {

    AUTH_BADCRED        (1,"Bogus credentials (seal broken)"),
    AUTH_REJECTEDCRED   (2,"Client should begin new session"),
    AUTH_BADVERF        (3,"Bogus verifier (seal broken)"),
    AUTH_REJECTEDVERF   (4,"Verifier expired or was replayed"),
    AUTH_TOOWEAK        (5,"Too weak"),
    AUTH_INVALIDRESP    (6,"Bogus response verifier"),
    AUTH_FAILED         (7,"Failued (reason unknown)"),
    RPCSEC_GSS_NOCRED   (13,"No credentials for user"),
    RPCSEC_GSS_FAILED   (14,"GSS failure, credentials deleted"),
    UNSPECIFIED         (-1,"Unspecified authentication failure")

    final int raw
    final String description

    static AuthFailureType fromInt(int i) {
        switch (i) {
            case 1:
                return AUTH_BADCRED
            case 2:
                return AUTH_REJECTEDCRED
            case 3:
                return AUTH_BADVERF
            case 4:
                return AUTH_REJECTEDVERF
            case 5:
                return AUTH_TOOWEAK
            case 6:
                return AUTH_INVALIDRESP
            case 7:
                return AUTH_FAILED
            case 13:
                return RPCSEC_GSS_NOCRED
            case 14:
                return RPCSEC_GSS_FAILED
            default:
                return UNSPECIFIED
        }
    }

    private AuthFailureType(int value,final String d) {
        raw = value
        description= d
    }

}
