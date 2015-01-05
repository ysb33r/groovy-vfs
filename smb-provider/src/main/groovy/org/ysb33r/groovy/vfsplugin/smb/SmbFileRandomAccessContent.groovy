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
package org.ysb33r.groovy.vfsplugin.smb

import groovy.transform.CompileStatic

import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.UnknownHostException

import jcifs.smb.SmbException
import jcifs.smb.SmbFile
import jcifs.smb.SmbRandomAccessFile

import org.apache.commons.vfs2.FileSystemException
import org.apache.commons.vfs2.provider.AbstractRandomAccessContent
import org.apache.commons.vfs2.util.RandomAccessMode

/**
 * RandomAccess for smb files
 */
@CompileStatic
class SmbFileRandomAccessContent extends AbstractRandomAccessContent
{
    private final SmbRandomAccessFile raf
    private final InputStream rafis

    SmbFileRandomAccessContent(final SmbFile smbFile, final RandomAccessMode mode)
    {
        super(mode)

        try {
            raf = new SmbRandomAccessFile(smbFile, mode.getModeString())
            rafis = new InputStream() {
                @Override
                int available() {
                    final long available = raf.length() - raf.getFilePointer();
                    return (available > Integer.MAX_VALUE) ? Integer.MAX_VALUE : available as int
                }

                @Override
                void close() { raf.close() }

                @Override
                int read()  { return raf.readByte() }

                @Override
                int read(final byte[] b) { return raf.read(b) }

                @Override
                int read(final byte[] b, final int off, final int len) { return raf.read(b, off, len) }

                @Override
                long skip(final long n) {
                    raf.seek(raf.getFilePointer() + n);
                    return n
                }
            };
        }
        catch (final MalformedURLException e) {
            throw new FileSystemException("vfs.provider/random-access-open-failed.error", smbFile, e);
        }
        catch (final SmbException e) {
            throw new FileSystemException("vfs.provider/random-access-open-failed.error", smbFile, e);
        }
        catch (final UnknownHostException e) {
            throw new FileSystemException("vfs.provider/random-access-open-failed.error", smbFile, e);
        }
    }

    InputStream getInputStream() { rafis }


    void    close()            { raf.close() }
    long    getFilePointer()   { raf.getFilePointer() }
    long    length()           { raf.length() }
    boolean readBoolean()      { raf.readBoolean() }
    byte    readByte()         { raf.readByte() }
    char    readChar()         { raf.readChar() }
    double  readDouble()       { raf.readDouble() }
    float   readFloat()        { raf.readFloat() }
    int     readInt()          { raf.readInt() }
    long    readLong()         { raf.readLong() }
    short   readShort()        { raf.readShort() }
    int     readUnsignedByte() { raf.readUnsignedByte() }
    int     readUnsignedShort(){ raf.readUnsignedShort() }
    String  readUTF()          { raf.readUTF() }

    void readFully(final byte[] b) {
        raf.readFully(b)
    }

    void readFully(final byte[] b, final int off, final int len) {
        raf.readFully(b, off, len)
    }

    void seek(final long pos) {
        raf.seek(pos)
    }

    void setLength(final long newLength)  {
        raf.setLength(newLength)
    }

    int skipBytes(final int n) {
        return raf.skipBytes(n)
    }

    @Override
    void write(final byte[] b) {
        raf.write(b)
    }

    @Override
    void write(final byte[] b, final int off, final int len) {
        raf.write(b, off, len)
    }

    @Override
    void write(final int b) {
        raf.write(b)
    }

    @Override
    void writeBoolean(final boolean v) {
        raf.writeBoolean(v)
    }

    @Override
    void writeByte(final int v) {
        raf.writeByte(v)
    }

    @Override
    void writeBytes(final String s) {
        raf.writeBytes(s)
    }

    @Override
    void writeChar(final int v) {
        raf.writeChar(v)
    }

    @Override
    void writeChars(final String s) {
        raf.writeChars(s)
    }

    @Override
    void writeDouble(final double v) {
        raf.writeDouble(v)
    }

    @Override
    void writeFloat(final float v) {
        raf.writeFloat(v)
    }

    @Override
    void writeInt(final int v) {
        raf.writeInt(v)
    }

    @Override
    void writeLong(final long v) {
        raf.writeLong(v)
    }

    @Override
    void writeShort(final int v) {
        raf.writeShort(v)
    }

    @Override
    void writeUTF(final String str) {
        raf.writeUTF(str)
    }

}
