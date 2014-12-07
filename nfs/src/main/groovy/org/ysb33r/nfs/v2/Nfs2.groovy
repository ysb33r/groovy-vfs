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

package org.ysb33r.nfs.v2

import groovy.transform.CompileStatic
import org.ysb33r.nfs.AbstractNfsVersion
import org.ysb33r.nfs.Buffer
import org.ysb33r.nfs.FileAttributes
import org.ysb33r.nfs.Nfs
import org.ysb33r.nfs.NfsCache
import org.ysb33r.nfs.NfsClient
import org.ysb33r.nfs.NfsException
import org.ysb33r.nfs.NfsSecurity
import org.ysb33r.rpc.cred.CredGss
import org.ysb33r.rpc.cred.CredUnix
import org.ysb33r.rpc.MsgRejectedException
import org.ysb33r.rpc.Rpc
import org.ysb33r.rpc.Xdr
import java.io.*
import static org.ysb33r.rpc.AuthFailureType.*
import static org.ysb33r.nfs.BufferState.*

/**
 * This class contains the methods specific to
 * NFS version 2.
 *
 * @see Nfs
 * @see Nfs3
 * @see FileAttributes
 * @author Brent Callaghan
 * @author Ricardo Labiaga
 */
@CompileStatic
class Nfs2 extends AbstractNfsVersion<FileAttributes2> {

    //Fattr2 attr;
    int nwb;	// current writes-behind

    /**
     * Construct a new NFS version 2 object (file or directory)
     *
     * @param rpc	Rpc object for the server
     * @param fh	File handle for the object
     * @param name	Name of the file/dir
     * @param attr	File attributes for the object
     */
    public Nfs2(Rpc rpc, byte[] fh, String name, FileAttributes2 fattr,NfsCache cache,CredUnix cred) {
        super(
            'rpc'      : rpc,
            fileHandle : fh,
            'name'     : name.startsWith("./") ? name.substring(2) : name,
            readSize   : RWSIZE,
            NRA        : 2,
            NWB        : 8,
            attr       : fattr ?: new FileAttributes2(),
            nfsCache   : cache,
            credentials  : cred
        )


    }

    @Override
    void syncFileAttributes() {
        Xdr call = new Xdr(readSize + 512);
        rpc.rpc_header(call, NFSPROC2_GETATTR);
        call.xdr_raw(fileHandle);

        Xdr reply;

        try {
            reply = rpc.rpc_call(call, 2 * 1000, 2);
        } catch (IOException e) {
            // don't let a mere syncFileAttributes hang
            // the app if the server is down.
            return;
        }

        int status = reply.xdr_int();
        if (status != NFS_OK)
            throw new NfsException(status);

        attr.getFattr(reply);
    }

    void checkAttr() {
        if (! attr.valid())
            syncFileAttributes()
    }

    boolean cacheOK(long t) {
        checkAttr()
        return t == attr.mtime;
    }

    void invalidate() {
	    attr.validtime = 0;
    }

    /*
     * Get the file modification time
     * @return the time in milliseconds
     */
    long mtime() {
        checkAttr()
        return attr.mtime
    }

    /*
     * Get the file size in bytes.
     *
     * Note that the size may be greater than that
     * shown in the attributes if the file is being written.
     *
     * @return file size
     */
    long length() {
        checkAttr()
        return maxLength > attr.size ? maxLength : attr.size
    }

    /*
     * Verify if file exists
     * @return true if file exists
     */
    boolean exists() {
        checkAttr()
        return true
    }

    private boolean check_access(long mode) {
        throw new IOException("check_access: NOT IMPLEMENTED YET")
//        boolean found = false;
//        long uid = NfsConnect.getCred().getUid();
//        long gid = NfsConnect.getCred().getGid();
//        int gids[] = NfsConnect.getCred().getGids();
//
//        /*
//         * Access check is based on only
//         * one of owner, group, public.
//         * If not owner, then check group.
//         * If not a member of the group,
//         * then check public access.
//         */
//        mode <<= 6;
//        if (uid != attr.uid) {
//            mode >>= 3;
//            if (gid != attr.gid) {
//                // check group list
//                int gidsLength = 0;
//
//                if (gids != null)
//                    gidsLength = gids.length;
//
//                for (int i = 0; i < gidsLength; i++)
//                    if (found = ((long)gids[i] == attr.gid))
//                        break;
//                if (!found) {
//                    // not in group list, check "other" field
//                    mode >>= 3;
//                }
//            }
//	    }
//
//	    return (attr.mode & mode) == mode;
    }

    /*
     * Verify if file can be created/updated
     * @return true if file can be created/updated
     */
    boolean canWrite() {
        checkAttr();

        return check_access(AbstractNfsVersion.WBIT);
    }

    /*
     * Verify if file can be read
     * @return true if file can be read
     */
    boolean canRead() {
        checkAttr();

        return check_access(AbstractNfsVersion.RBIT);
    }

    /*
     * Verify if this is a file
     * @return true if a file
     */
    boolean isFile() {
        checkAttr()
        return attr.ftype == AbstractNfsVersion.NFREG
    }

    /*
     * Verify if this is a directory 
     * @return true if a directory
     */
    boolean isDirectory() {
        checkAttr()
        return attr.ftype == AbstractNfsVersion.NFDIR
    }

    /*
     * Verify if this is a symbolic link
     * @return true if a symbolic link
     */
    boolean isSymlink() {
        checkAttr()
        return attr.ftype == AbstractNfsVersion.NFLNK
    }

    /*
     * @return file attributes
     */
    FileAttributes getAttr() {
        checkAttr()
        return attr as FileAttributes
    }

    /*
     * Lookup a name in a directory
     *
     * If its a symbolic link - follow it
     *
     * @param name	Name of entry in directory
     * @returns		Nfs object
     * @exception java.io.IOException
     */
    @Override
    Nfs lookup(String name) {
        byte[] newfh;
        FileAttributes2 newattrs;
        Nfs nfs;
        String pathname;

	/* For multi-component lookup, the name would already be
	 * filled in when object is created and 
	 * thus name passed in will be null.
	 */ 
	if (name == null) {
		pathname = this.name;
		name = this.name;
	} else { /* Single component case  */
            if (this.name == null)
                pathname = name;
            else
                pathname = this.name + "/" + name;
	}

        /*
         * First check the cache to see
         * if we already have this file/dir
         */
        nfs = cache_get(rpc.conn.server, pathname);
        if (nfs != null && nfs.cacheOK(cacheTime)) {

	    // If a symbolic link then follow it

            if (((Nfs2)nfs).attr.ftype == AbstractNfsVersion.NFLNK)
                nfs = NfsClient.followLink(nfs);

            return nfs;
        }

        Xdr call = new Xdr(readSize + 512);
	Xdr reply = null;

	/*
	 * If needed, give one try to get the security information
	 * from the server.
	 */
	for (int sec_tries = 1; sec_tries >= 0; sec_tries--) {
	    rpc.rpc_header(call, NFSPROC2_LOOKUP);
	    call.xdr_raw(fileHandle);
	    call.xdr_string(name);

	    try {
		reply = rpc.rpc_call(call, 5 * 1000, 0);
                break;
            } catch (MsgRejectedException e) {
		/*
		 * Check if this lookup is using public fh.
		 * If so and if the call receives an AUTH_TOOWEAK error,
		 * lookupSec() is called to get the security flavor
		 * information from the server by using the WebNFS
		 * security negotiation ip (supported in Solaris 8).
		 */
		boolean is_v2pubfh = true;
		for (int i = 0; i < 32; i++) {
		    if (fileHandle[i] != (byte) 0) {
			is_v2pubfh = false;
			break;
		    }
		}
                if (is_v2pubfh && e.why == AUTH_TOOWEAK) {
                    String secKey = lookupSec();
                    if (secKey != null &&
			NfsSecurity.getMech(secKey) != null) {
			rpc.setCred(new CredGss("nfs",
                                NfsSecurity.getMech(secKey),
                                NfsSecurity.getService(secKey),
                                NfsSecurity.getQop(secKey)));
                        continue;
                    } else if (secKey != null && secKey.equals("1")) {
			rpc.setCred(new CredUnix());
			continue;
		    }
                }    
                throw e;
            } catch (IOException e) {
                throw e;
            }
	} // for

        int status = reply.xdr_int();
        if (status != NFS_OK)
            throw new NfsException(status);

        newfh = reply.xdr_raw(FHSIZE);
        newattrs = new FileAttributes2(reply);

        nfs = createFrom( newfh, pathname, newattrs)
        cache_put(nfs);

	// If a symbolic link then follow it

        if (((Nfs2)nfs).attr.ftype == AbstractNfsVersion.NFLNK)
            nfs = NfsClient.followLink(nfs);

        return nfs;
    }

    /*
     *  lookupSec() uses the WebNFS security negotiation ip to
     *  get nfs flavor numbers required by the nfs server.
     *
     *  If the server fails to return the security numbers, the client
     *  will use a default security mode specified in the
     *  nfssec.properties file.
     *
     *  If the server successfully returns a list of security modes,
     *  the client will use the preferred security mode that matches
     *  any security number found in the list, otherwise, it
     *  will use the first security number from the list that the
     *  client supports.
     *
     *  Null string is returned if the client does not support any
     *  security numbers that the server requests.
     *
     *  Here is an example of the WebNFS security negotiation ip:
     *
     *	    Suppose the server shares /export/home as follows:
     *
     *           share -o sec=sec_1:sec_2:sec_3 /export/secure
     *
     *	    Here is how to READ a file from server:/export/secure:
     *
     *         Client                                  Server
     *          ------                                  ------
     *
     *       LOOKUP 0x0, foo, "path"
     *                              ----------->
     *                              <-----------
     *                                                  AUTH_TOOWEAK
     *
     *       LOOKUP 0x0, foo, 0x81, <sec_index>, "path"
     *                              ----------->
     *                              <-----------
     *                                          FH = {...,sec_1, ..., sec_3}
     *       LOOKUP 0x0, foo, "path"
     *		use a selected sec flavor (sec_x)
     *                              ----------->
     *                              <-----------
     *                                          FH = 0x01
     *       READ 0x01, sec_x, offset=0 for 32k
     *                              ----------->
     *                              <-----------
     *
     *  Here is the contents of the FH returned from the server for the
     *  "0x81" V2 LOOKUP request:
     *
     *   1   2   3   4                                          32
     * +---+---+---+---+---+---+---+---+   +---+---+---+---+   +---+
     * | l | s |   |   |     sec_1     |...|     sec_n     |...|   |
     * +---+---+---+---+---+---+---+---+   +---+---+---+---+   +---+
     *
     *  where l is the length : 4 * n (bytes)
     *        s is status indicating whether there are more sec flavors
     *		(1 is yes, 0 is no) that require the client to perform
     *		another 0x81 LOOKUP request. Client uses <sec_index>
     *		to indicate the offset of the flavor number (sec_index + n)
     *
     */
    String lookupSec() {

        throw new IOException("lookupSpec: NOT IMPLEMENTED YET")
//        int sec_index = 1;
//        boolean more = false;
//        String secmode, first_secmode = null;
//        Xdr call = new Xdr(readSize + 512);
//
//        do {
//            rpc.rpc_header(call, NFSPROC2_LOOKUP);
//            call.xdr_raw(new byte[32]);  // v2 public file handle
//
//            // send "0x81pathname" over the wire
//            int len = name.getBytes().length + 2;
//            byte[] b = new byte[len];
//            b[0] = (byte) 0x81;
//            b[1] = (byte) sec_index;
//            System.arraycopy(name.getBytes(), 0, b, 2, name.getBytes().length);
//            call.xdr_bytes(b);
//
//            Xdr reply = rpc.rpc_call(call, 5 * 1000, 3);
//            int status = reply.xdr_int();
//
//            // If the server does not support the MClookup security
//            // negotiation, simply use the default security flavor.
//            if (status != NFS_OK) {
//                return NfsSecurity.getDefault();
//            }
//
//            byte[] s = reply.xdr_raw(4);
//            int numsec = ((int) s[0])/4; // 4 is sizeof (int)
//            if (s[1] == (byte) 0) {
//                more = false;
//            } else {
//                more = true;
//                sec_index = sec_index + numsec;
//            }
//
//            String prefer = NfsSecurity.getPrefer();
//            while (numsec-- > 0) {
//                secmode = Integer.toString(reply.xdr_int());
//
//                if ((prefer != null) && prefer.equals(secmode)) {
//                    return prefer;
//                }
//
//                if ((first_secmode == null) &&
//                        NfsSecurity.hasValue(secmode)) {
//                    first_secmode = secmode;
//                }
//            }
//        } while (more);
//
//        return first_secmode;
    }


    /*
     * Read data from a file into a buffer
     *
     */
    void read_otw(Buffer buf) {

        Xdr call = new Xdr(readSize + 512);

        rpc.rpc_header(call, NFSPROC2_READ);
        call.xdr_raw(fileHandle);
        call.xdr_u_int(buf.foffset);
        call.xdr_u_int(readSize);
        call.xdr_u_int(readSize);	// totalcount (unused)

        Xdr reply = rpc.rpc_call(call, 1 * 1000, 0);

        int status = reply.xdr_int();
        if (status != NFS_OK)
            throw new NfsException(status);

        attr.getFattr(reply);

        int bytesread = reply.xdr_int();
	    buf.eof = buf.foffset + readSize >= attr.size;
        buf.buf = reply.xdr_buf();
        buf.bufoff = reply.xdr_offset();
	    buf.buflen = bytesread;
        cacheTime = attr.mtime;
    }

    /*
     * Write data from a file into a buffer
     *
     */
    int write_otw(Buffer buf) {

        Xdr call = new Xdr(writeSize + 512);

        int fileOffset = (int) buf.foffset + buf.minOffset;
        int writeLength = buf.maxOffset - buf.minOffset;

        rpc.rpc_header(call, NFSPROC2_WRITE);
        call.xdr_raw(fileHandle);
        call.xdr_u_int(fileOffset);	// beginoffset - not used
        call.xdr_u_int(fileOffset);
        call.xdr_u_int(writeLength);	// totalcount - not used
        call.xdr_bytes(buf.buf, buf.bufoff + buf.minOffset, writeLength);
    
        Xdr reply = rpc.rpc_call(call, 2 * 1000, 0);

        int status = reply.xdr_int();
        if (status != NFS_OK)
   		throw new NfsException(status);

        attr.getFattr(reply);

        buf.status = LOADED;
        buf.writeVerifier = 0;
        cacheTime = attr.mtime;

        return writeLength;
    }

    /*
     * Read a directory
     *
     * Returns an array of names.
     */
    String[] readdir() {

        long cookie = 0;
	    boolean eof = false;
        String[] s = new String[32];
        String ename;
        int i = 0;

        /*
         * If we already have the directory entries
         * cached then return them.
         */
        if (dircache != null && cacheOK(cacheTime))
            return dircache;

        Xdr call = new Xdr(readSize + 512);

        while (!eof) {
            rpc.rpc_header(call, NFSPROC2_READDIR);
            call.xdr_raw(fileHandle);
            call.xdr_u_int(cookie);
            call.xdr_u_int(4096);	// number of directory bytes
    
            Xdr reply = rpc.rpc_call(call, 2 * 1000, 0);
    
            int status = reply.xdr_int();
            if (status != NFS_OK)
                throw new NfsException(status);
    
            /*
             * Get directory entries
             */
            while (reply.xdr_bool()) {
                reply.xdr_u_int();		// skip fileid
                ename = reply.xdr_string();	// filename

                cookie = reply.xdr_u_int();

                if (ename.equals(".") || ename.equals(".."))	// ignore
                    continue;

                s[i++] = ename;

                if (i >= s.length) {		// last elem in array ?
                    String[] tmp = s;

                    s = new String[i*2];	// double its size
                    System.arraycopy(tmp, 0, s, 0, i);
                }
            }
            eof = reply.xdr_bool();	// end of directory
        }

        /*
         * Trim array to exact size
         */
        if (i < s.length) {
            String[] tmp = s;

            s = new String[i];
            System.arraycopy(tmp, 0, s, 0, i);
        }

        dircache = s;
        cacheTime = attr.mtime;

        return s;
    }

    /*
     * Read a symbolic link
     *
     * Returns the text in the symbolic link
     */
    String readlink() {
        /*
         * If we've already read the symlink
         * then return the cached text.
         */
        if (symlink != null && cacheOK(cacheTime))
            return symlink;

        Xdr call = new Xdr(readSize + 512);
        rpc.rpc_header(call, NFSPROC2_READLINK);
        call.xdr_raw(fileHandle);
    
        Xdr reply = rpc.rpc_call(call, 2 * 1000, 0);
    
        int status = reply.xdr_int();
        if (status != NFS_OK)
            throw new NfsException(status);

        symlink = reply.xdr_string();
        cacheTime = attr.mtime;

        return symlink;
    }

    /*
     * Create a file
     *
     * @param name	Name of file
     * @param mode	UNIX style mode
     * @returns		true if successful
     * @exception java.io.IOException
     */
    Nfs create(String name, long mode) {
	    create_otw(NFSPROC2_CREATE, name, mode)
    }

    /*
     * Create a directory
     *
     * @param name	name of directory
     * @param mode	UNIX style mode
     * @returns 	true if successful
     * @exception java.io.IOException
     */
    Nfs mkdir(String name, long mode) {
	    create_otw(NFSPROC2_MKDIR, name, mode)
    }

    /*
     * Create Nfs Object over-the-wire
     *
     * @param nfsOp	NFS operation
     * @param mode	UNIX style mode
     * @returns		true if successful
     * @exception java.io.IOException
     */
    private Nfs create_otw(int nfsOp, String name, long mode) {

        long currTime = System.currentTimeMillis();
        byte[] newfh;
        FileAttributes2 newattrs;
        Nfs nfs;

        Xdr call = new Xdr(readSize + 512)
        rpc.rpc_header(call, nfsOp)
        call.xdr_raw(fileHandle)
        call.xdr_string(name)
        call.xdr_u_int(mode)
        call.xdr_u_int(credentials.uid)	// owner
        call.xdr_u_int(credentials.gid)	// group
        call.xdr_u_int(0)			// size
        call.xdr_u_int( (currTime / 1000) as long )	// atime seconds
        call.xdr_u_int(currTime % 1000)	// atime mseconds
        call.xdr_u_int(currTime / 1000)	// mtime seconds
        call.xdr_u_int(currTime % 1000)	// mtime mseconds

        Xdr reply = rpc.rpc_call(call, 2 * 1000, 0)

        int status = reply.xdr_int()
        if (status != NFS_OK)
            throw new NfsException(status)

        /*
         * Cache the new NFS Object
         */
        newfh = reply.xdr_raw(FHSIZE);
        newattrs = new FileAttributes2(reply);

        String pathname = this.name + "/" + name;

        nfs = createFrom(newfh, pathname, newattrs)
        cache_put(nfs);
        dircache = null;
        return nfs;
    }

    /*
     * Get Filesystem Information
     *
     */
    void fsinfo() {
        writeSize = RWSIZE;
    }

    /*
     * Commit writes - not implemented in v2
     */
    long commit(int foffset, int length) {
        return 0;
    }

    /**
     * Remove file
     *
     * Returns true if the file was removed
     */
    boolean remove(String name) {
	return remove_otw(NFSPROC2_REMOVE, name);
    }

    /**
     * Remove directory
     *
     * @returns true if the directory could be deleted
     * @exception java.io.IOException
     */
    boolean rmdir(String name) {
	return remove_otw(NFSPROC2_RMDIR, name);
    }

    /*
     * Remove Nfs Object
     *
     * @param nfsOp over-the-wire operation
     * @param nfsP  Nfs object of Parent directory
     * @param name  Name of file/dir to be deleted.
     * @return true if the Nfs Object could be deleted
     * @exception java.io.IOException
     */
    private boolean remove_otw(int nfsOp, String name) {

	Xdr call = new Xdr(readSize + 512);
	rpc.rpc_header(call, nfsOp);
	call.xdr_raw(fileHandle)
	call.xdr_string(name);

	Xdr reply = rpc.rpc_call(call, 2 * 1000, 0);

	int status = reply.xdr_int();
	if (status != NFS_OK)
	    throw new NfsException(status);

	cache_remove(this, name);	// Remove Nfs object from cache
	dircache = null;
	return true;
    }

    /*
     * Rename file
     *
     * @param srcP	Nfs obj of parent of src
     * @param dstP	Nfs obj of parent of dst
     * @param sName	src Name.s
     * @param dName	destination filename. May be 'path/filename' or simply
     * 			'filename'
     * @returns true if the file/directory was renamed
     * @exception java.io.IOException
     */
    boolean rename(Nfs destination, String sName, String dName){

    Nfs2 dstP = destination as Nfs2
	Xdr call = new Xdr(readSize + 512);

	rpc.rpc_header(call, NFSPROC2_RENAME);
	call.xdr_raw(fileHandle)			// Source dir filehandle
	call.xdr_string(sName);			// Source filename
	call.xdr_raw(dstP.fileHandle)		// Dest dir filehandle
	call.xdr_string(dName);			// Dest filename

	Xdr reply = rpc.rpc_call(call, 2 * 1000, 0);

	int status = reply.xdr_int();
	if (status != NFS_OK)
	    throw new NfsException(status);

	cache_remove(this, sName);	// Remove Nfs object from cache
	dircache = null;
	dstP.dircache = null;
	return true;
    }

    @Override
    protected Nfs createFrom(byte[] fh, String name, FileAttributes2 attr) {
        new Nfs2(rpc,fh,name,attr as FileAttributes2,nfsCache,credentials)
    }

    @Override
    protected FileAttributes2 createAttr() {
        new FileAttributes2()
    }

    /*
     * NFS version 2 procedure numbers
     */
    private final static int NFSPROC2_NULL        = 0;
    private final static int NFSPROC2_GETATTR     = 1;
    private final static int NFSPROC2_SETATTR     = 2;
    private final static int NFSPROC2_LOOKUP      = 4;
    private final static int NFSPROC2_READLINK    = 5;
    private final static int NFSPROC2_READ        = 6;
    private final static int NFSPROC2_WRITE       = 8;
    private final static int NFSPROC2_CREATE      = 9;
    private final static int NFSPROC2_REMOVE      = 10;
    private final static int NFSPROC2_RENAME      = 11;
    private final static int NFSPROC2_LINK        = 12;
    private final static int NFSPROC2_SYMLINK     = 13;
    private final static int NFSPROC2_MKDIR       = 14;
    private final static int NFSPROC2_RMDIR       = 15;
    private final static int NFSPROC2_READDIR     = 16;
    private final static int NFSPROC2_STATFS      = 17;

    private final static int NFS_OK = 0;
    private final static int RWSIZE = 8192;	// optimal read/write size
    private final static int FHSIZE = 32;	// file handle size

}
