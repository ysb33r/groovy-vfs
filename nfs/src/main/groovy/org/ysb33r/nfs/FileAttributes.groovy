/*
 * This code is strongly based upon original YANFS. Original license
 * kept below as per requirement.
 *
 * Copyright (c) 2014 Schalk W. Cronjé.
 * All  Rights Reserved.
 */
/*
 * Copyright (c) 1998, 2007 Sun Microsystems, Inc. 
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

package org.ysb33r.nfs

import groovy.transform.ToString
import org.ysb33r.rpc.Xdr

/**
 *
 * NFS file attributes
 *
 * This is essentially a container class that holds
 * the attributes, but it also has methods to encode
 * and decode the attributes in an Xdr buffer.
 *
 * Note that the time at which the attributes were
 * retrieved is automatically updated and the cache
 * time varies according to the frequency of file
 * modification.
 *
 * There are two subclasses: Fattr2 for NFS v2
 * attributes and Fattr3 for v3.
 *
 * @see Nfs
 * @see org.ysb33r.nfs.v2.FileAttributes2
 * @see org.ysb33r.nfs.v3.FileAttributes3
 * @author Brent Callaghan
 * @author Schalk Cronjé
 */
@ToString(includeNames=true,includePackage = false)
abstract class FileAttributes {

    /** Minimum cache time of 3s
    */
    static final int ACMIN = 3  * 1000

    /** Maximum cache time of 60s
    */
    static final int ACMAX = 60 * 1000

    /** SVR4 UID/GUID 'nobody'
    */
    static final int NOBODY = 60001

    /** NFS UID/GUID 'nobody'
     */
    static final int NFS_NOBODY = -2

    /** Time when attributes were considered new
     */
    long validtime

    /** Maximum cache duration in ms
     */
    long cachetime


    int		ftype
    long 	mode
    long	nlink
    long	uid
    long	gid
    long	size
    long	rdev
    long	fsid
    long	fileid
    long	atime
    long	mtime
    long	ctime

    /**
     * Check if the attributes are "fresh" enough.
     *
     * If not, then the caller will likely update
     * the attributes with an NFS syncFileAttributes request.
     *
     * @returns	true if the attributes are valid
     */
    boolean valid() {
        long timenow = System.currentTimeMillis()

        return (timenow <= validtime + cachetime)
    }
        
    abstract void putFattr(Xdr x)
    abstract void getFattr(Xdr x)
}
