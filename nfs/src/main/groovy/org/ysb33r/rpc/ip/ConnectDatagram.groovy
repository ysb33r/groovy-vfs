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

package org.ysb33r.rpc.ip

import groovy.transform.CompileStatic
import org.ysb33r.rpc.Connection
import org.ysb33r.rpc.Xdr

import java.io.*;
import static org.ysb33r.rpc.Protocol.UDP


/**
 * Sets up a UDP connection to the server.
 * Since UDP is really connectionless, we
 * don't really have a connection, so perhaps
 * describing this as an <i>association</i>
 * is more accurate.
 *
 * This class lets us transmit and receive buffers
 * of data to a port on a remote server.
 *
 * @see Connection
 * @author Brent Callaghan
 */
@CompileStatic
class ConnectDatagram extends Connection {

    DatagramSocket ds
    DatagramPacket dp
    InetAddress addr

    /**
     * Construct a new connection to a specified server and port.
     * @param server	The hostname of the server
     * @param port	The port number on the server
     * @param maxSize	The maximum size in bytes of the received message
     * @exception IOException if the server does not exist
     */
    public ConnectDatagram (String server, int port, int maxSize)
        throws IOException {

        super(server, port,UDP, maxSize);

        ds = new DatagramSocket();
        addr = InetAddress.getByName(server);
        start();
    }

    void sendOne(Xdr x) {
        ds.send(new DatagramPacket(x.xdr_buf(), x.xdr_offset(), addr, port));
    }

    void receiveOne(Xdr x, int timeout) throws IOException {
        ds.setSoTimeout(timeout);
        dp = new DatagramPacket(x.xdr_buf(), x.xdr_buf().length);
        ds.receive(dp);
    }

    InetAddress getPeer() {
        return dp.getAddress();
    }

    /*
     * No connection to drop.
     */
    void dropConnection() {
    }

    /*
     * No connection to check
     */
    void checkConnection() {
    }

    protected void finalize() {
        if (ds != null) {
            ds.close();
            ds = null;
        }
        super.finalize();
    }
}
