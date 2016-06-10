/**
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2015
 *
 * This software is licensed under the Apache License 2.0
 * See http://www.apache.org/licenses/LICENSE-2.0 for license details
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 * ============================================================================
 */

// ----------------------------------------------------------------------------
//  Much of this code is a modficiation or a port of original Java code from the
//  Apache Commons VFS project and therefore the following license may still apply
// ----------------------------------------------------------------------------
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ysb33r.groovy.vfsplugin.cpio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VfsLog;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.bzip2.Bzip2FileObject;

/**
 * A read-only file system for Cpio files.
 */
public class CpioFileSystem extends AbstractFileSystem
{
    private static final int DEFAULT_INDEX_SIZE = 100;

    private static final Log LOG = LogFactory.getLog(CpioFileSystem.class);

    private final File file;
    private CpioArchiveInputStream cpioFile;

    protected CpioFileSystem(final AbstractFileName rootName,
                             final FileObject parentLayer,
                             final FileSystemOptions fileSystemOptions)
        throws FileSystemException
    {
        super(rootName, parentLayer, fileSystemOptions);

        // Make a local copy of the file
        file = parentLayer.getFileSystem().replicateFile(parentLayer, Selectors.SELECT_SELF);

        // Open the Cpio file
        if (!file.exists())
        {
            // Don't need to do anything
            cpioFile = null;
            return;
        }

        // cpioFile = createCpioFile(this.file);
    }

    @Override
    public void init() throws FileSystemException
    {
        super.init();

        // Build the index
        try
        {
            final List<CpioFileObject> strongRef = new ArrayList<CpioFileObject>(DEFAULT_INDEX_SIZE);
            CpioArchiveEntry entry;

            while ((entry = getCpioFile().getNextCPIOEntry()) != null)
            {
                final AbstractFileName name = (AbstractFileName) getFileSystemManager().resolveName(getRootName(),
                        UriParser.encode(entry.getName()));

                // Create the file
                CpioFileObject fileObj;
                if (entry.isDirectory() && getFileFromCache(name) != null)
                {
                    fileObj = (CpioFileObject) getFileFromCache(name);
                    fileObj.setCpioEntry(entry);
                    continue;
                }

                fileObj = createCpioFileObject(name, entry);
                putFileToCache(fileObj);
                strongRef.add(fileObj);
                fileObj.holdObject(strongRef);

                // Make sure all ancestors exist
                // TODO - create these on demand
                CpioFileObject parent = null;
                for (AbstractFileName parentName = (AbstractFileName) name.getParent();
                     parentName != null;
                     fileObj = parent, parentName = (AbstractFileName) parentName.getParent())
                {
                    // Locate the parent
                    parent = (CpioFileObject) getFileFromCache(parentName);
                    if (parent == null)
                    {
                        parent = createCpioFileObject(parentName, null);
                        putFileToCache(parent);
                        strongRef.add(parent);
                        parent.holdObject(strongRef);
                    }

                    // Attach child to parent
                    parent.attachChild(fileObj.getName());
                }
            }
        }
        catch (final IOException e)
        {
            throw new FileSystemException(e);
        }
        finally
        {
            closeCommunicationLink();
        }
    }

    public InputStream getInputStream(final CpioArchiveEntry entry) throws FileSystemException
    {
        resetCpioFile();
        try
        {
            while (!cpioFile.getNextEntry().equals(entry))
            {
            }
            return cpioFile;
        }
        catch (final IOException e)
        {
            throw new FileSystemException(e);
        }
    }

    protected void resetCpioFile() throws FileSystemException
    {
        // Reading specific entries requires skipping through the Cpio file from the beginning
        // Not especially elegant, but we don't have the ability to seek to specific positions
        // with an input stream.
        if (this.file.exists())
        {
            recreateCpioFile();
        }
    }

    private void recreateCpioFile() throws FileSystemException
    {
        if (this.cpioFile != null)
        {
            try
            {
                this.cpioFile.close();
            }
            catch (final IOException e)
            {
                throw new FileSystemException("vfs.provider.Cpio/close-Cpio-file.error", file, e);
            }
            cpioFile = null;
        }

        final CpioArchiveInputStream CpioFile = createCpioFile(this.file);
        this.cpioFile = CpioFile;
    }

    protected CpioArchiveInputStream getCpioFile() throws FileSystemException
    {
        if (cpioFile == null && this.file.exists())
        {
            recreateCpioFile();
        }

        return cpioFile;
    }

    protected CpioFileObject createCpioFileObject(final AbstractFileName name,
                                                final CpioArchiveEntry entry) throws FileSystemException
    {
        return new CpioFileObject(name, entry, this, true);
    }

    protected CpioArchiveInputStream createCpioFile(final File file) throws FileSystemException
    {
        try
        {
            if ("cpiogz".equalsIgnoreCase(getRootName().getScheme()))
            {
                return new CpioArchiveInputStream(new GZIPInputStream(new FileInputStream(file)));
            }
            else if ("cpiobz2".equalsIgnoreCase(getRootName().getScheme()))
            {
                return new CpioArchiveInputStream(Bzip2FileObject.wrapInputStream(file.getAbsolutePath(),
                    new FileInputStream(file)));
            }
            return new CpioArchiveInputStream(new FileInputStream(file));
        }
        catch (final IOException ioe)
        {
            throw new FileSystemException("vfs.provider.Cpio/open-Cpio-file.error", file, ioe);
        }
    }

    @Override
    protected void doCloseCommunicationLink()
    {
        // Release the Cpio file
        try
        {
            if (cpioFile != null)
            {
                cpioFile.close();
                cpioFile = null;
            }
        }
        catch (final IOException e)
        {
            // getLogger().warn("vfs.provider.Cpio/close-Cpio-file.error :" + file, e);
            VfsLog.warn(getLogger(), LOG, "vfs.provider.Cpio/close-Cpio-file.error :" + file, e);
        }
    }

    /**
     * Returns the capabilities of this file system.
     */
    @Override
    protected void addCapabilities(final Collection<Capability> caps)
    {
        caps.addAll(CpioFileProvider.capabilities);
    }

    /**
     * Creates a file object.
     */
    @Override
    protected FileObject createFile(final AbstractFileName name) throws FileSystemException
    {
        // This is only called for files which do not exist in the Cpio file
        return new CpioFileObject(name, null, this, false);
    }

    /**
     * will be called after all file-objects closed their streams.
    protected void notifyAllStreamsClosed()
    {
        closeCommunicationLink();
    }
     */
}
