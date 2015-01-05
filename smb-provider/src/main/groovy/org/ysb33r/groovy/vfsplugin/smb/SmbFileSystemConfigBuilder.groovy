//
// ============================================================================
// (C) Copyright Schalk W. Cronje 2013-2015
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// //
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// //
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//
// ============================================================================
//

package org.ysb33r.groovy.vfsplugin.smb

import groovy.transform.CompileStatic
import org.apache.commons.vfs2.FileSystemConfigBuilder

class SmbFileSystemConfigBuilder extends FileSystemConfigBuilder {

    static final def OPTIONS = [ name : 'TYPE' ]

    @Override
    @CompileStatic
    Class<? extends FileSystem> getConfigClass() { SmbFileSystem.class }

    SmbFileSystemConfigBuilder() {super('smb.')}

    SmbFileSystemConfigBuilder(final String prefix) {super(prefix)}

    // missingMethods
    // if not set/get MissingMethodException
    // if in OPTIONS
    //   "get${TYPE}"( opts, SmbFileSystemConfigBuilder.class.name + ${NAME} )
    //   setParam( opts, SmbFileSystemConfigBuilder.class.name + ${NAME}, VALUE )
}

//    private static final String _PREFIX = FtpFileSystemConfigBuilder.class.getName();
//public Boolean getPassiveMode(final FileSystemOptions opts)
//{
//    return getBoolean(opts, PASSIVE_MODE);
//}
//private static final String PASSIVE_MODE = _PREFIX + ".PASSIVE";
//   public void setConnectTimeout(final FileSystemOptions opts, final Integer connectTimeout)
//{
//    setParam(opts, CONNECT_TIMEOUT, connectTimeout);
//}//
