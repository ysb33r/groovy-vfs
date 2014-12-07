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

import java.util.Collection

import org.apache.commons.vfs2.Capability
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystemException
import org.apache.commons.vfs2.FileSystemOptions
import org.apache.commons.vfs2.provider.AbstractFileName
import org.apache.commons.vfs2.provider.AbstractFileSystem

/**
 * An SMB file system.
 */
@CompileStatic
class SmbFileSystem extends AbstractFileSystem
{
    protected SmbFileSystem(final FileName rootName, final FileSystemOptions fileSystemOptions) {
        super(rootName, null, fileSystemOptions)
    }

    /**
     * Creates a file object.
     */
    @Override
    protected FileObject createFile(final AbstractFileName name) throws FileSystemException {
        return new SmbFileObject(name, this)
    }

    /**
     * Returns the capabilities of this file system.
     */
    @Override
    protected void addCapabilities(Collection<Capability> caps) {
        Collection<Capability> cc = SmbFileProvider.CAPABILITIES
        caps.addAll(cc)
    }
}
