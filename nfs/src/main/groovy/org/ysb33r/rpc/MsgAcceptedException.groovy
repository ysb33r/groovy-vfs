/*
 * This code is strongly based upon original YANFS. Original license
 * kept below as per requirement.
 *
 * Copyright (c) 2014 Schalk W. Cronjé.
 * All  Rights Reserved.
 */
/*
 * Copyright (c) 1997, 2007 Sun Microsystems, Inc. 
 * All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * -Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS
 * SHALL NOT BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE
 * AS A RESULT OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE
 * SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE
 * LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED
 * AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed,licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

package org.ysb33r.rpc

/**
 *
 * Handle the RPC "message accepted" class of errors.
 *
 * Note that some of the errors also convey low and high
 * version information.
 *
 * @see RpcException
 * @author Brent Callaghan
 * @author Schalk W. Cronjé
 */
 class MsgAcceptedException extends RpcException {
    final AcceptStatus status
    final Integer lo = null
    final Integer hi = null

    /*
     * Construct a new Exception for the specified RPC accepted error
     * @param error	The RPC error number
     */
    MsgAcceptedException(AcceptStatus error) {
        super(error.raw)
        status = error
    }

    /*
     * Construct a new RPC error with the given low and high parameters
     * @param error	The RPC error number
     * @param lo	The low version number
     * @param hi	The high version number
     */
    MsgAcceptedException(AcceptStatus error, int lo_, int hi_) {
        super(error.raw)
        status = error
        lo = lo_
        hi = hi_
    }

    String toString() {
        if(status==AcceptStatus.UNSPECIFIED) {
            return "Unknown RPC Error = " + error
        } else if(lo != null && hi != null) {
            return "${status.description}: low=${lo}, high=${hi}"
        } else {
            return status.description
        }
    }
}
