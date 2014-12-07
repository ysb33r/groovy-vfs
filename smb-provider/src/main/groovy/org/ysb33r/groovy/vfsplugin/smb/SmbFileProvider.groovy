// ============================================================================
// (C) Copyright Schalk W. Cronje 2014
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
// ============================================================================

package org.ysb33r.groovy.vfsplugin.smb

import groovy.transform.CompileStatic

import java.util.Arrays
import java.util.Collection
import java.util.Collections

import org.apache.commons.vfs2.Capability
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileSystem
import org.apache.commons.vfs2.FileSystemException
import org.apache.commons.vfs2.FileSystemOptions
import org.apache.commons.vfs2.UserAuthenticationData
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider
import org.apache.commons.vfs2.provider.FileProvider
import static org.apache.commons.vfs2.Capability.*
import static org.apache.commons.vfs2.UserAuthenticationData.*

/**
 * A provider for SMB (Samba, Windows share) file systems.
 */
@CompileStatic
class SmbFileProvider extends AbstractOriginatingFileProvider implements FileProvider
{
    final static Collection<Capability> CAPABILITIES = [
        CREATE,
        DELETE,
        RENAME,
        GET_TYPE,
        GET_LAST_MODIFIED,
        SET_LAST_MODIFIED_FILE,
        SET_LAST_MODIFIED_FOLDER,
        LIST_CHILDREN,
        READ_CONTENT,
        Capability.URI,
        WRITE_CONTENT,
        APPEND_CONTENT,
        RANDOM_ACCESS_READ,
        RANDOM_ACCESS_WRITE
    ]

    final static UserAuthenticationData.Type[] AUTHENTICATOR_TYPES = [USERNAME,PASSWORD,DOMAIN] as UserAuthenticationData.Type[]

    SmbFileProvider() {
        super()
        setFileNameParser(SmbFileNameParser.getInstance())
    }

    /**
     * Creates the filesystem.
     */
    @Override
    protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions) {
        new SmbFileSystem(name, fileSystemOptions)
    }

    Collection<Capability> getCapabilities() { CAPABILITIES }
}
