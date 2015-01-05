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
import org.apache.commons.vfs2.provider.FileNameParser
import org.apache.commons.vfs2.provider.HostFileNameParser
import org.apache.commons.vfs2.provider.URLFileNameParser
import org.apache.commons.vfs2.provider.UriParser
import org.apache.commons.vfs2.provider.VfsComponentContext


@CompileStatic
class SmbFileNameParser extends URLFileNameParser
{
    private final static SmbFileNameParser INSTANCE = new SmbFileNameParser()

    SmbFileNameParser() {
        super(139)
    }

    static FileNameParser getInstance() { INSTANCE }

    @Override
    public FileName parseUri(final VfsComponentContext context, final FileName base, final String filename)
    {
        final StringBuilder name = new StringBuilder();

        // Extract the scheme and authority parts
        final HostFileNameParser.Authority auth = extractToPath(filename, name);

        // extract domain
        String username = auth.getUserName();
        final String domain = extractDomain(username);
        if (domain != null)
        {
            username = username.substring(domain.length() + 1);
        }

        // Decode and adjust separators
        UriParser.canonicalizePath(name, 0, name.length(), this);
        UriParser.fixSeparators(name);

        // Extract the share
        final String share = UriParser.extractFirstElement(name);
        if (share == null || share.length() == 0)
        {
            throw new FileSystemException("vfs.provider.smb/missing-share-name.error", filename);
        }

        // Normalise the path.  Do this after extracting the share name,
        // to deal with things like smb://hostname/share/..
        final FileType fileType = UriParser.normalisePath(name);
        final String path = name.toString();

        new SmbFileName(
            auth.scheme,
            auth.hostName,
            auth.port,
            username,
            auth.password,
            domain,
            share,
            path,
            fileType)
    }

    private String extractDomain(final String username) {
        if (username != null) {

            for (int i = 0; i < username.length(); i++) {
                if (username.charAt(i) == '\\') {
                    return username.substring(0, i);
                }
            }
        }
        return null;
    }
}
