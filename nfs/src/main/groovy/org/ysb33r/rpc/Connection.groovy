/*
 * This code is strongly based upon original YANFS. Original license
 * kept below as per requirement.
 *
 * Copyright (c) 2014 Schalk W. Cronjé.
 * All  Rights Reserved.
 */

/*
 * Copyright (c) 1999, 2007 Sun Microsystems, Inc. 
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

import groovy.transform.CompileStatic
import groovy.transform.Synchronized
import groovy.util.logging.Slf4j

import java.io.*

/**
 * Sets up a connection to the server using
 * either UDP or TCP as determined by the
 * subclass.
 *
 * This class also handles the connection caching.
 *
 * @see org.ysb33r.rpc.ip.ConnectSocket
 * @see org.ysb33r.rpc.ip.ConnectDatagram
 * @author Brent Callaghan
 */
@CompileStatic
@Slf4j
abstract class Connection extends Thread {

    String server
    int port
    Protocol proto
    int maxSize;	// size of reply Xdr buffer

    Hashtable waiters = new Hashtable();
    static final int IDLETIME = 300 * 1000; // idle connection after 5 min
    int xid;		// transaction id
    Xdr reply;
    Error err;		// might get thrown by the thread

    /**
     * Construct a new connection to a specified <i>server</i>
     * and <i>port</i> using ip <i>proto</i> with a
     * reply buffer of size <i>maxsize</i>.
     *
     * @param server	The hostname of the server
     * @param port	The port number on the server
     */
    Connection (final String server, int port, Protocol proto, int maxSize) {
        this.server = server
        this.port = port
        this.proto = proto
        this.maxSize = maxSize

        setName("Listener-" + server)
        setDaemon(true)
    }

    abstract void sendOne(Xdr call)
    abstract void receiveOne(Xdr reply, int timeout)
    abstract InetAddress getPeer()
    abstract void dropConnection()
    abstract void checkConnection()

//
//    /**
//     * Return information about the connection
//     *
//     * @returns server, port number and ip info.
//     */
//    public String toString() {
//        return (server + ":" + port + ":" + proto);
//    }
//
//
//
//
    @Synchronized
    Xdr send(Xdr call, int timeout) {
throw new IOException("NOT IMPLEMENTED YET")
//        checkConnection()
//        resumeListener()
//        sendOne(call)
//
//        waiters.put(new Integer(call.xid), new Integer(timeout));
//
//        /*
//         * Now sleep until the listener thread posts
//         * my XID and notifies me - or I time out.
//         */
//        while (xid != call.xid) {
//            long t = System.currentTimeMillis();
//
//	    if (err != null)
//		throw err;
//
//            try {
//                wait(timeout);
//            } catch (InterruptedException e) {}
//
//	    if (err != null)
//		throw err;
//
//            timeout -= (System.currentTimeMillis() - t);
//            if (timeout <= 0) {
//                waiters.remove(new Integer(call.xid));
//                throw new InterruptedIOException(); // timed out
//            }
//        }
//
//        /*
//         * My reply has come in.
//         */
//        xid = 0;
//        waiters.remove(new Integer(call.xid));
//        notifyAll(); // wake the listener
//
//        return reply;
    }

    /*
     * This is the code for the listener thread.
     * It blocks in a receive waiting for an RPC
     * reply to come in, then delivers it to the
     * appropriate thread.
     */
    public void run() {

        try {
            while(true) {
                waitForTransactionId()
                reply = new Xdr(maxSize)
                if(waitForPacketOrTimeout()) {
                    extractTransactionId()
                }
            }
        } catch (Error e) {
            // Need to catch errors here, e.g. OutOfMemoryError and notify threads before this listener thread dies
            // otherwise they'll wait forever.
            recordError(e)
            throw e
        }
    }

    @Synchronized
    private void waitForTransactionId() {
        while (xid != 0) {
            try {
                wait()
            } catch (InterruptedException e) {}
        }
    }

    private boolean waitForPacketOrTimeout() {
        try {
            receiveOne(reply, IDLETIME);
        } catch (InterruptedIOException e) {
             // Got an idle timeout.  If there's no threads waiting then drop the connection and suspend.
            if (waiters.isEmpty()) {
                dropConnection()
            }

            suspendListener()
        } catch (IOException e) {
            return false
        }

        return true
    }


    /** Having received an Xdr buffer, extract the xid and check the hashtable to see if there's thread waiting for that
     * reply. If there is, then notify the thread.  If not then ignore the reply (its thread may have timed out and
     * gone away).
     */
    @Synchronized
    private void extractTransactionId() {
        xid = reply.xdr_int()
        if (waiters.containsKey(new Integer(xid))) {
            notifyAll()
        } else {
            xid = 0   // ignore it
        }
    }

    @Synchronized
    private void recordError(final Error e) {
        this.err = e;
        notifyAll();
    }

    @Synchronized
    private void suspendListener() {
        running = false

        while (!running) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
    }

    @Synchronized
    private void resumeListener() {
        running = true
        notifyAll()
    }

    private boolean running
}
