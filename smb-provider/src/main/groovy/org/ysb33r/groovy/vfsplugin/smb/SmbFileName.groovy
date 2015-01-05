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
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileSystemException
import org.apache.commons.vfs2.FileType
import org.apache.commons.vfs2.provider.GenericFileName


/**
 * An SMB URI.  Adds a share name to the generic URI.
 */
@CompileStatic
class SmbFileName extends GenericFileName
{
    private static final int DEFAULT_PORT = 139

    final String share
    final String domain
    private String uriWithoutAuth

    protected SmbFileName(
        final String scheme,
        final String hostName,
        final int port,
        final String userName,
        final String password,
        final String domain,
        final String share,
        final String path,
        final FileType type)
    {
        super(scheme, hostName, port, DEFAULT_PORT, userName, password, path, type)
        this.share = share
        this.domain = domain
    }

    /**
     * Builds the root URI for this file name.
     */
    @Override
    protected void appendRootUri(final StringBuilder buffer, final boolean addPassword)
    {
        super.appendRootUri(buffer, addPassword);
        buffer.append('/');
        buffer.append(share);
    }

    /**
     * put domain before username if both are set
     */
    @Override
    protected void appendCredentials(final StringBuilder buffer, final boolean addPassword)
    {
        if (getDomain() != null && getDomain().length() != 0 &&
            getUserName() != null && getUserName().length() != 0)
        {
            buffer.append(getDomain());
            buffer.append("\\");
        }
        super.appendCredentials(buffer, addPassword);
    }

    /**
     * Factory method for creating name instances.
     */
    @Override
    public FileName createName(final String path, final FileType type)
    {
        return new SmbFileName(
            getScheme(),
            getHostName(),
            getPort(),
            getUserName(),
            getPassword(),
            domain,
            share,
            path,
            type);
    }

    /**
     * Construct the path suitable for SmbFile when used with NtlmPasswordAuthentication
     */
    String getUriWithoutAuth() throws FileSystemException
    {
        if (uriWithoutAuth != null)
        {
            return uriWithoutAuth;
        }

        final StringBuilder sb = new StringBuilder(120);
        sb.append(getScheme());
        sb.append("://");
        sb.append(getHostName());
        if (getPort() != DEFAULT_PORT)
        {
            sb.append(":");
            sb.append(getPort());
        }
        sb.append("/");
        sb.append(getShare());
        sb.append(getPathDecoded());
        uriWithoutAuth = sb.toString();
        return uriWithoutAuth;
    }
}
