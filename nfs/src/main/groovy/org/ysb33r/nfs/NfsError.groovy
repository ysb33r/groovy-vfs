package org.ysb33r.nfs

/**
 * Created by schalkc on 02/08/2014.
 */
enum NfsError {

    /** Everything is OK
     * 
     */
    NFS_OK (0),

    /** Caller not privileged
     * 
     */
    NFSERR_PERM (1),
    NFSERR_NOENT (2),
    NFSERR_IO (5),
    NFSERR_NXIO (6),
    NFSERR_ACCES (13),
    NFSERR_EXIST (17),
    NFSERR_XDEV (18),
    NFSERR_NODEV (19),
    NFSERR_NOTDIR (20),
    NFSERR_ISDIR (21),
    NFSERR_INVAL (22),
    NFSERR_FBIG (27),
    NFSERR_NOSPC (28),
    NFSERR_ROFS (30),
    NFSERR_MLINK (31),
    NFSERR_NAMETOOLONG (63),
    NFSERR_NOTEMPTY (66),
    NFSERR_DQUOT (69),
    NFSERR_STALE (70),
    NFSERR_REMOTE (71),
    NFSERR_BADHANDLE (10001),
    NFSERR_NOT_SYNC (10002),
    NFSERR_BAD_COOKIE (10003),
    NFSERR_NOTSUPP (10004),
    NFSERR_TOOSMALL (10005),
    NFSERR_SERVERFAULT (10006),
    NFSERR_BADTYPE (10007),
    NFSERR_JUKEBOX (10008),

    // New error codes added by NFS v4

    /** nverify says attrs same
    */
    NFS4ERR_SAME (10009),

    /** lock unavailable
     */
    NFS4ERR_DENIED (10010),

    /** lock lease expired
     */
    NFS4ERR_EXPIRED (10011),

    /** I/O failed due to lock
     */
    NFS4ERR_LOCKED (10012),

    /** in grace period
     */
    NFS4ERR_GRACE (10013),

    /* filehandle expired
     */
    NFS4ERR_FHEXPIRED (10014),

    /** share reserve denied
     */
    NFS4ERR_SHARE_DENIED (10015),

    /** wrong security flavor
     *
     */
    NFS4ERR_WRONGSEC (10016),

    /** clientid in use
     */
    NFS4ERR_CLID_INUSE (10017)

//    NFS4ERR_PERM            = 1,    /* caller not privileged   */
//    NFS4ERR_NOENT           = 2,    /* no such file/directory  */
//    NFS4ERR_IO              = 5,    /* hard I/O error          */
//    NFS4ERR_NXIO            = 6,    /* no such device          */
//    NFS4ERR_ACCESS          = 13,   /* access denied           */
//    NFS4ERR_EXIST           = 17,   /* file already exists     */
//    NFS4ERR_XDEV            = 18,   /* different filesystems   */
    /* Unused/reserved        19 */
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

    NfsError(int e) {error=e}

    int error
}
