// ============================================================================
// (C) Copyright Schalk W. Cronje 2014
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// This code is strongly based upon original Java code found at
//
//   http://svn.apache.org/viewvc/commons/proper/vfs/trunk/sandbox
//
// to which the below license applies. Modification and distribution of
// this code is incompliance with the Apache 2.0 license
//
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// ============================================================================
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
