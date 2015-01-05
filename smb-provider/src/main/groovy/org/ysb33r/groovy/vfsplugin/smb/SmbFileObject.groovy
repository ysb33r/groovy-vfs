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
package org.ysb33r.groovy.vfsplugin.smb

import groovy.transform.CompileStatic

import java.io.InputStream
import java.io.OutputStream
import java.net.MalformedURLException

import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbException
import jcifs.smb.SmbFile
import jcifs.smb.SmbFileInputStream
import jcifs.smb.SmbFileOutputStream

import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystemException
import org.apache.commons.vfs2.FileType
import org.apache.commons.vfs2.FileTypeHasNoContentException
import org.apache.commons.vfs2.RandomAccessContent
import org.apache.commons.vfs2.UserAuthenticationData
import org.apache.commons.vfs2.provider.AbstractFileName
import org.apache.commons.vfs2.provider.AbstractFileObject
import org.apache.commons.vfs2.provider.UriParser
import org.apache.commons.vfs2.util.RandomAccessMode
import org.apache.commons.vfs2.util.UserAuthenticatorUtils

/**
 * A file in an SMB file system.
 */
@CompileStatic
class SmbFileObject extends AbstractFileObject //<SmbFileSystem>
{
    private SmbFile file

    protected SmbFileObject(final AbstractFileName name, final SmbFileSystem fileSystem) {
        super(name, fileSystem);
    }

    /**
     * Attaches this file object to its file resource.
     */
    @Override
    protected void doAttach()
    {
        // Defer creation of the SmbFile to here
        if (file == null)
        {
            file = createSmbFile(getName());
        }
    }

    @Override
    protected void doDetach() throws Exception
    {
        // file closed through content-streams
        file = null;
    }

    private SmbFile createSmbFile(final FileName fileName) throws MalformedURLException, SmbException, FileSystemException
    {
        final SmbFileName smbFileName = (SmbFileName) fileName;

        final String path = smbFileName.getUriWithoutAuth();

        UserAuthenticationData authData = null;
        SmbFile file;
        NtlmPasswordAuthentication auth;
        try
        {
            authData = UserAuthenticatorUtils.authenticate(getFileSystem().getFileSystemOptions(), SmbFileProvider.AUTHENTICATOR_TYPES);

            auth = new NtlmPasswordAuthentication(
                UserAuthenticatorUtils.toString(
                    UserAuthenticatorUtils.getData(
                        authData,
                        UserAuthenticationData.DOMAIN,
                        UserAuthenticatorUtils.toChar(smbFileName.getDomain()))),
                UserAuthenticatorUtils.toString(
                    UserAuthenticatorUtils.getData(
                        authData,
                        UserAuthenticationData.USERNAME,
                        UserAuthenticatorUtils.toChar(smbFileName.getUserName()))),
                UserAuthenticatorUtils.toString(
                    UserAuthenticatorUtils.getData(
                        authData,
                        UserAuthenticationData.PASSWORD,
                        UserAuthenticatorUtils.toChar(smbFileName.getPassword()))));

            file = new SmbFile(path, auth);
        }
        finally
        {
            UserAuthenticatorUtils.cleanup(authData);
        }

        if (file.isDirectory() && !file.toString().endsWith("/"))
        {
            file = new SmbFile(path + "/", auth);
        }

        return file;
    }

    /**
     * Determines the type of the file, returns null if the file does not
     * exist.
     */
    @Override
    protected FileType doGetType() throws Exception
    {
        if (!file.exists())
        {
            return FileType.IMAGINARY;
        }
        else if (file.isDirectory())
        {
            return FileType.FOLDER;
        }
        else if (file.isFile())
        {
            return FileType.FILE;
        }

        throw new FileSystemException("vfs.provider.smb/get-type.error", getName());
    }

    /**
     * Lists the children of the file.  Is only called if {@link #doGetType}
     * returns {@link FileType#FOLDER}.
     */
    @Override
    protected String[] doListChildren() throws Exception
    {
        // VFS-210: do not try to get listing for anything else than directories
        if (!file.isDirectory())
        {
            return null;
        }

        return UriParser.encode(file.list());
    }

    /**
     * Determines if this file is hidden.
     */
    @Override
    protected boolean doIsHidden() throws Exception
    {
        return file.isHidden();
    }

    /**
     * Deletes the file.
     */
    @Override
    protected void doDelete() throws Exception
    {
        file.delete();
    }

    @Override
    protected void doRename(final FileObject newfile) throws Exception
    {
        file.renameTo(createSmbFile(newfile.getName()));
    }

    /**
     * Creates this file as a folder.
     */
    @Override
    protected void doCreateFolder() throws Exception
    {
        file.mkdir();
        file = createSmbFile(getName());
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    @Override
    protected long doGetContentSize() throws Exception
    {
        return file.length();
    }

    /**
     * Returns the last modified time of this file.
     */
    @Override
    protected long doGetLastModifiedTime()
        throws Exception
    {
        return file.getLastModified();
    }

    /**
     * Creates an input stream to read the file content from.
     */
    @Override
    protected InputStream doGetInputStream() throws Exception
    {
        try
        {
            return new SmbFileInputStream(file)
        }
        catch (final SmbException e)
        {
//            if (e.ntStatus == SmbException.NT_STATUS_NO_SUCH_FILE)
//            {
//                throw new org.apache.commons.vfs2.FileNotFoundException(getName())
//            }
            if (file.isDirectory())
            {
                throw new FileTypeHasNoContentException(getName())
            }

            throw e
        }
    }

    /**
     * Creates an output stream to write the file content to.
     */
    @Override
    protected OutputStream doGetOutputStream(final boolean bAppend) throws Exception
    {
        return new SmbFileOutputStream(file, bAppend);
    }

    /**
     * random access
     */
    @Override
    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception
    {
        return new SmbFileRandomAccessContent(file, mode);
    }

    @Override
    protected boolean doSetLastModifiedTime(final long modtime) throws Exception {
        file.setLastModified(modtime);
        return true;
    }
}
