/*
 * This code is strongly based upon original YANFS. Original license
 * kept below as per requirement.
 *
 * Copyright (c) 2014 Schalk W. Cronjé.
 * All  Rights Reserved.
 */
/*
 * Copyright (c) 1997-1999, 2007 Sun Microsystems, Inc. 
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

/**
 * This exception is thrown whenever an NFS error occurs.
 * NFS v2 & v3 share the same error codes.
 * NFS v4 shared most of those codes
 */
class NfsException extends java.io.IOException {


    static final int NFS_OK = 0
    static final int NFSERR_PERM = 1
    static final int NFSERR_NOENT = 2
    static final int NFSERR_IO = 5
    static final int NFSERR_NXIO = 6
    static final int NFSERR_ACCES = 13
    static final int NFSERR_EXIST = 17
    static final int NFSERR_XDEV = 18
    static final int NFSERR_NODEV = 19
    static final int NFSERR_NOTDIR = 20
    static final int NFSERR_ISDIR = 21
    static final int NFSERR_INVAL = 22
    static final int NFSERR_FBIG = 27
    static final int NFSERR_NOSPC = 28
    static final int NFSERR_ROFS = 30
    static final int NFSERR_MLINK = 31
    static final int NFSERR_NAMETOOLONG = 63
    static final int NFSERR_NOTEMPTY = 66
    static final int NFSERR_DQUOT = 69
    static final int NFSERR_STALE = 70
    static final int NFSERR_REMOTE = 71
    static final int NFSERR_BADHANDLE = 10001
    static final int NFSERR_NOT_SYNC = 10002
    static final int NFSERR_BAD_COOKIE = 10003
    static final int NFSERR_NOTSUPP = 10004
    static final int NFSERR_TOOSMALL = 10005
    static final int NFSERR_SERVERFAULT = 10006
    static final int NFSERR_BADTYPE = 10007
    static final int NFSERR_JUKEBOX = 10008

//    NFS4_OK                 = 0,    /* everything is okay      */
//    NFS4ERR_PERM            = 1,    /* caller not privileged   */
//    NFS4ERR_NOENT           = 2,    /* no such file/directory  */
//    NFS4ERR_IO              = 5,    /* hard I/O error          */
//    NFS4ERR_NXIO            = 6,    /* no such device          */
//    NFS4ERR_ACCESS          = 13,   /* access denied           */
//    NFS4ERR_EXIST           = 17,   /* file already exists     */
//    NFS4ERR_XDEV            = 18,   /* different filesystems   */
//    /* Unused/reserved        19 */
//    NFS4ERR_NOTDIR          = 20,   /* should be a directory   */
//    NFS4ERR_ISDIR           = 21,   /* should not be directory */
//    NFS4ERR_INVAL           = 22,   /* invalid argument        */
//    NFS4ERR_FBIG            = 27,   /* file exceeds server max */
//    NFS4ERR_NOSPC           = 28,   /* no space on filesystem  */
//    NFS4ERR_ROFS            = 30,   /* read-only filesystem    */
//    NFS4ERR_MLINK           = 31,   /* too many hard links     */
//    NFS4ERR_NAMETOOLONG     = 63,   /* name exceeds server max */
//    NFS4ERR_NOTEMPTY        = 66,   /* directory not empty     */
//    NFS4ERR_DQUOT           = 69,   /* hard quota limit reached*/
//    NFS4ERR_STALE           = 70,   /* file no longer exists   */
//    NFS4ERR_BADHANDLE       = 10001,/* Illegal filehandle      */
//    NFS4ERR_BAD_COOKIE      = 10003,/* READDIR cookie is stale */
//    NFS4ERR_NOTSUPP         = 10004,/* operation not supported */
//    NFS4ERR_TOOSMALL        = 10005,/* response limit exceeded */
//    NFS4ERR_SERVERFAULT     = 10006,/* undefined server error  */
//    NFS4ERR_BADTYPE         = 10007,/* type invalid for CREATE */
//    NFS4ERR_DELAY           = 10008,/* file "busy" - retry     */
//    NFS4ERR_SAME            = 10009,/* nverify says attrs same */
//    NFS4ERR_DENIED          = 10010,/* lock unavailable        */
//    NFS4ERR_EXPIRED         = 10011,/* lock lease expired      */
//    NFS4ERR_LOCKED          = 10012,/* I/O failed due to lock  */
//    NFS4ERR_GRACE           = 10013,/* in grace period         */
//    NFS4ERR_FHEXPIRED       = 10014,/* filehandle expired      */
//    NFS4ERR_SHARE_DENIED    = 10015,/* share reserve denied    */
//    NFS4ERR_WRONGSEC        = 10016,/* wrong security flavor   */
//    NFS4ERR_CLID_INUSE      = 10017,/* clientid in use         */
    int error

    /**
     * Create a new NfsException
     *
     * @param NFS error number for this error
     */
    NfsException(int error) {
        super("NFS error: " + error)
        this.error = error
    }

    String toString() {

        switch (error) {
            case NFS_OK:
                return ("OK")
            case NFSERR_PERM:
                return ("Not owner")
            case NFSERR_NOENT:
                return ("No such file or directory")
            case NFSERR_IO:
                return ("I/O error")
            case NFSERR_NXIO:
                return ("No such device or address")
            case NFSERR_ACCES:
                return ("Permission denied")
            case NFSERR_EXIST:
                return ("File exists")
            case NFSERR_XDEV:
                return ("Attempted cross-device link")
            case NFSERR_NODEV:
                return ("No such device")
            case NFSERR_NOTDIR:
                return ("Not a directory")
            case NFSERR_ISDIR:
                return ("Is a directory")
            case NFSERR_INVAL:
                return ("Invalid argument")
            case NFSERR_FBIG:
                return ("File too large")
            case NFSERR_NOSPC:
                return ("No space left on device")
            case NFSERR_ROFS:
                return ("Read-only file system")
            case NFSERR_MLINK:
                return ("Too many links")
            case NFSERR_NAMETOOLONG:
                return ("File name too long")
            case NFSERR_NOTEMPTY:
                return ("Directory not empty")
            case NFSERR_DQUOT:
                return ("Disk quota exceeded")
            case NFSERR_STALE:
                return ("Stale NFS file handle")
            case NFSERR_REMOTE:
                return ("Too many levels of remote in path")
            case NFSERR_BADHANDLE:
                return ("Illegal NFS file handle")
            case NFSERR_NOT_SYNC:
                return ("Update sync mismatch")
            case NFSERR_BAD_COOKIE:
                return ("Readdir cookie is stale")
            case NFSERR_NOTSUPP:
                return ("Operation not supported")
            case NFSERR_TOOSMALL:
                return ("Buffer/request too small")
            case NFSERR_SERVERFAULT:
                return ("Server fault")
            case NFSERR_BADTYPE:
                return ("Bad type")
            case NFSERR_JUKEBOX:
                return ("Jukebox error: try later")
        }
        return ("Unknown NFS error: " + error)
    }
}
