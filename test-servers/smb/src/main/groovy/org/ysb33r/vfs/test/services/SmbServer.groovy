/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2015
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * //
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * //
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * ============================================================================
 */

package org.ysb33r.vfs.test.services

import groovy.transform.CompileStatic
import org.alfresco.jlan.app.JLANCifsServer

class SmbServer implements Runnable {

    static final String CONFIGXML = System.getProperty('JLANCONFIG') ?: 'src/test/resources/jlanserver.xml'
    static final String ROOT = System.getProperty('ROOT') ?: '.'
    static final def CONFIG = new XmlSlurper().parse(new File(CONFIGXML))
    static final String HOSTNAME = CONFIG.SMB.host.bindto.text()
    static final String PORT = CONFIG.SMB.host.tcpipSMB.@port
    static final String DOMAIN = CONFIG.SMB.host.@domain
    static final String USER = CONFIG.security.users.user[1].@name
    static final String PASSWORD = CONFIG.security.users.user[1].password.text()
    static final String READSHARE = CONFIG.shares.diskshare[0].@name
    static final String READDIR = new File(CONFIG.shares.diskshare[0].driver.LocalPath.text()).absoluteFile
    static final String WRITESHARE = CONFIG.shares.diskshare[1].@name

    void run() {
        JLANCifsServer.main(CONFIGXML)
    }

    void start() {
        new Thread(this).start()
        sleep(2000)
    }

    void stop() {
        JLANCifsServer.shutdownServer(CONFIGXML)
    }
}