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
