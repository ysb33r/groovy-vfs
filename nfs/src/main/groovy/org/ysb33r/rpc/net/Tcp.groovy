package org.ysb33r.rpc.net

import java.nio.channels.Channels
import java.nio.channels.SocketChannel

//import static org.ysb33r.rpc.Protocol.TCP
import static java.net.StandardSocketOptions.*

/**
 * Created by schalkc on 05/08/2014.
 */
class Tcp {

    final String server
    final int port
    final int maxReplySize

    /**
     * Construct a new connection to a specified server and port.
     *
     * @param server	The hostname of the server
     * @param port	The port number on the server
     * @param maxSize	The maximum size of the received reply
     * @exception IOException if the connection cannot be made
     */
    Tcp (final String server, int port, int maxSize) {

        this.server = server
        this.port = port
        this.maxReplySize = maxSize
//        super(server, port, TCP, maxSize);
        doConnect()
//        start();	// the listener
    }

    void receiveOne() {
        input.read()
    }

    private void doConnect() {

        if (server == null) {
            throw new java.net.UnknownHostException("null host")
        }

        socket = SocketChannel.open( new InetSocketAddress(server,port) ) //new Socket(server, port)
        socket.setOption(TCP_NODELAY)
        input = Channels.newInputStream(socket)
        output = Channels.newOutputStream(socket)
    }

    private SocketChannel socket
    private OutputStream output
    private InputStream  input
}
