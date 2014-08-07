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

package org.ysb33r.rpc

import groovy.transform.CompileStatic;

import java.io.*;

/**
 * This class handles the marshalling/unmarshalling of
 * primitive data types into and out of a buffer.
 *
 * The XDR buffer is a field within this class and its
 * size is determined when the class is instantiated.
 * Other than this buffer, there are just two pointers:
 * "off" is the current XDR offset into the buffer and
 * moves up the buffer by an integral number of XDRUNITs
 * as data are encoded/decoded.  The other pointer is
 * "size" which is the number of valid data bytes in
 * the buffer and is set only for received buffers.
 *
 * XXX we should perhaps check that off <= size
 * whenever an item is decoded so that we can raise
 * an exception if the received data is underlength.
 *
 * @see Rpc
 * @author Brent Callaghan
 * @author Schalk W. Cronjé
 */
@CompileStatic
class Xdr {
    int xid

    /**
     * Build a new Xdr object with a buffer of given size
     *
     * @param size	of the buffer in bytes
     */
    Xdr(int size) {
        this.buf = new byte[size];
        this.size = size;
        this.off = 0;
    }

    /**
     * Skip a number of bytes.
     * <br>Note that the count is
     * rounded up to the next XDRUNIT.
     *
     * @param count	of the buffer in bytes
     */
    void xdr_skip(int count) {
        int r = (off += count) % XDRUNIT

        if (r > 0)
            off += XDRUNIT - r
    }

    /**
     * Return the entire Xdr buffer
     *
     * @return	Xdr buffer
     */
    byte[] xdr_buf() { buf }

    /**
     * Return the current offset
     *
     * @return	offset
     */
    int xdr_offset() { off }


    /**
     * Set the current offset
     *
     * @param	off offset into XDR buffer
     */
    void xdr_offset(int off) { this.off = off }

    /**
     * Return the starting point of the bytes that will
     * be encrypted.
     *
     * @return	offset for bytes to be encrypted
     */
    int xdr_wrap_offset() { wrap_offset }

    /**
     * Set the starting point of the bytes that will
     * be encrypted.
     *
     * @return	offset for bytes to be encrypted
     */
    void xdr_wrap_offset(int off) { wrap_offset = off }

    /**
     * Return the current size of the XDR buffer
     *
     * @return	size
     */
    int xdr_size() { size }

    /**
     * Set the current size of the XDR buffer
     *
     * @param	size of buffer
     */
    void xdr_size(int size) { this.size = size }

    /**
     * Get an integer from the buffer
     *
     * @return integer
     */
    int xdr_int() {
        (buf[off++] & 0xff) << 24 |
        (buf[off++] & 0xff) << 16 |
        (buf[off++] & 0xff) << 8  |
        (buf[off++] & 0xff)
    }

    /**
     * Put an integer into the buffer
     *
     * @param i Integer to store in XDR buffer.
     */
    void xdr_int(int i) {
	    buf[off++] = (byte)(i >>> 24)
	    buf[off++] = (byte)(i >> 16)
	    buf[off++] = (byte)(i >> 8)
	    buf[off++] = (byte)i
    }

    /**
     * Get an unsigned integer from the buffer
     *
     * <br>Note that Java has no unsigned integer
     * type so we must return it as a long.
     *
     * @return long
     */
    int xdr_u_int() {
	    ((buf[off++] & 0xff) << 24 |
        (buf[off++] & 0xff) << 16 |
        (buf[off++] & 0xff) << 8  |
        (buf[off++] & 0xff));
    }

    /**
     * Put an unsigned integer into the buffer
     *
     * Note that Java has no unsigned integer
     * type so we must submit it as a long.
     *
     * @param i unsigned integer to store in XDR buffer.
     */
    void xdr_u_int(long i) {
        buf[off++] = (byte)(i >>> 24 & 0xff)
        buf[off++] = (byte)(i >> 16)
        buf[off++] = (byte)(i >> 8)
        buf[off++] = (byte)i
    }

    /**
     * Get a long from the buffer
     *
     * @return long
     */
    long xdr_hyper() {
	    (long)(buf[off++] & 0xff) << 56 |
        (long)(buf[off++] & 0xff) << 48 |
        (long)(buf[off++] & 0xff) << 40 |
        (long)(buf[off++] & 0xff) << 32 |
        (long)(buf[off++] & 0xff) << 24 |
        (long)(buf[off++] & 0xff) << 16 |
        (long)(buf[off++] & 0xff) << 8  |
        (long)(buf[off++] & 0xff)
    }
//
    /**
     * Put a long into the buffer
     *
     * @param i long to store in XDR buffer
     */
    void xdr_hyper(long i) {
        buf[off++] = (byte)(i >>> 56) 
        buf[off++] = (byte)((i >> 48) & 0xff)
        buf[off++] = (byte)((i >> 40) & 0xff)
        buf[off++] = (byte)((i >> 32) & 0xff)
        buf[off++] = (byte)((i >> 24) & 0xff)
        buf[off++] = (byte)((i >> 16) & 0xff)
        buf[off++] = (byte)((i >> 8) & 0xff)
        buf[off++] = (byte)(i & 0xff)
    }

    /*
     * Note: we have no XDR routines for encoding/decoding
     * unsigned longs.  They exist in XDR but not in Java
     * hence we can't represent them.
     * Best just to use xdr_hyper() and hope the sign bit
     * isn't used.
     */

    /**
     * Get a boolean from the buffer
     *
     * @return boolean
     */
    boolean xdr_bool() { xdr_int() != 0 }

    /**
     * Put a boolean into the buffer
     *
     * @param b boolean
     */
    void xdr_bool(boolean b) { xdr_int(b ? 1 : 0) }

    /**
     * Get a floating point number from the buffer
     *
     * @return float
     */
    float xdr_float() { Float.intBitsToFloat(xdr_int()) }

    /**
     * Put a floating point number into the buffer
     *
     * @param f float
     */
    void xdr_float(float f) { xdr_int(Float.floatToIntBits(f)) }

    /**
     * Get a string from the buffer
     *
     * @return string
     */
    String xdr_string() {
        int len = xdr_int()

        String s = new String(buf, off, len)
        xdr_skip(len)
        return s
    }

    /**
     * Put a string into the buffer
     *
     * @param s string
     */
    void xdr_string(final String s) { xdr_bytes(s.getBytes()) }

    /**
     * Get a counted array of bytes from the buffer
     *
     * @return bytes
     */
    byte[] xdr_bytes() { xdr_raw(xdr_int()) }

    /**
     * Put a counted array of bytes into the buffer.
     * Note that the entire byte array is encoded.
     *
     * @param	b byte array
     */
    void xdr_bytes(byte[] b) { xdr_bytes(b, 0, b.length) }

    /**
     * Put a counted array of bytes into the buffer
     *
     * @param	b byte array
     * @param	len number of bytes to encode
     */
    void xdr_bytes(byte[] b, int len) { xdr_bytes(b, 0, len) }

    /**
     * Put a counted array of bytes into the buffer
     *
     * @param	b byte array
     * @param	boff offset into byte array
     * @param	len number of bytes to encode
     */
    void xdr_bytes(byte[] b, int boff, int len) {
	    xdr_int(len)
	    System.arraycopy(b, boff, buf, off, len)
	    xdr_skip(len)
    }

    /**
     * Put an Xdr buffer into the buffer
     *
     * <br> This is used to encode the RPC credentials
     *
     * @param	x XDR buffer
     */
    void xdr_bytes(Xdr x) { xdr_bytes(x.xdr_buf(), x.xdr_offset()) }

    /**
     * Get a fixed number of bytes from the buffer
     *
     * e.g. an NFS v2 filehandle
     *
     * @param len	Number of bytes to get
     * @return byte array
     */
    byte[] xdr_raw(int len) {
        if (len == 0)
            return null

        byte[] b = new byte[len]
        System.arraycopy(buf, off, b, 0, len)
        xdr_skip(len)
        return b
    }

    /**
     * Get a fixed number (len) of bytes from the buffer
     * at offset off.  Do not change any buffer indicators.
     *
     * @param off	Offset of bytes to get from
     * @param len	Number of bytes to copy
     * @return byte array
     */
    byte[] xdr_raw(int off, int len) {
        if (len == 0)
            return null;

        byte[] b = new byte[len];

        System.arraycopy(buf, off, b, 0, len);
        return b;
    }

    /**
     * Put a fixed number of bytes into the buffer
     * The length is not encoded.
     *
     * e.g. an NFS v2 filehandle
     *
     * @param b byte array
     */
    void xdr_raw(byte[] b) {
    	int len = b.length
	    System.arraycopy(b, 0, buf, off, len)
	    xdr_skip(len)
    }

    /**
     * Put a fixed number of bytes into the buffer
     * at offset off.  The length is not encoded.
     *
     * @param b byte array
     * @param off where to put the byte array
     */
    void xdr_raw(byte[] b, int off) {
    	int len = b.length
	    System.arraycopy(b, 0, buf, off, len)
	    xdr_skip(len)
    }

    /**
     * Put a counted array of bytes into the buffer.
     * The length is not encoded.
     *
     * @param   b byte array
     * @param   boff offset into byte array
     * @param   len number of bytes to encode
     */
    void xdr_raw(byte[] b, int boff, int len) {
        System.arraycopy(b, boff, buf, off, len)
        xdr_skip(len)
    }

    private static int XDRUNIT = 4
    private byte[] buf
    private int size, off, wrap_offset

}
