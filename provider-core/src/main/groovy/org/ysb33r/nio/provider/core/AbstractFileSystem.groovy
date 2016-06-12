package org.ysb33r.nio.provider.core

import groovy.transform.CompileStatic

import java.nio.file.ClosedFileSystemException
import java.nio.file.FileSystem
import java.nio.file.spi.FileSystemProvider

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
abstract class AbstractFileSystem extends FileSystem {

    AbstractFileSystem(FileSystemProvider provider) {
        fileSystemProvider = provider
    }

    /**
     * Returns the provider that created this file system.
     *
     * @return The provider that created this file system.
     */
    @Override
    FileSystemProvider provider() {
        fileSystemProvider
    }

    /**
     * Tells whether or not this file system is open.
     *
     * @return {@code true} if, and only if, this file system is open
     */
    @Override
    boolean isOpen() {
        open
    }

    /**
     * Closes this file system.
     *
     * <p> After a file system is closed then all subsequent access to the file
     * system, either by methods defined by this class or on objects associated
     * with this file system, throw {@link java.nio.file.ClosedFileSystemException}. If the
     * file system is already closed then invoking this method has no effect.
     *
     * <p> Closing a file system will close all open {@link
     * java.nio.channels.Channel channels}, {@link DirectoryStream directory-streams},
     * {@link java.nio.file.WatchService watch-service}, and other closeable objects associated
     * with this file system.
     *
     * @throws IOException
     *          If an I/O error occurs
     * @throws UnsupportedOperationException
     *          Thrown in the case of the default file system
     */
    @Override
    void close()  {
        if(isOpen()) {
            try {
                doCloseFileSystem()
            }
            finally {
                open = false
            }
        }
    }

    /**
     * Returns the name separator, represented as a string.
     *
     * <p> The name separator is used to separate names in a path string. An
     * implementation may support multiple name separators in which case this
     * method returns an implementation specific <em>default</em> name separator.
     * This separator is used when creating path strings by invoking the {@link
     * java.nio.file.Path # toString ( ) toString()} method.
     *
     * @return The name separator
     */
    @Override
    String getSeparator() {
        return '/'
    }

    /**
     * Performs the physical actions of closing a filesystem.
     *
     * <p> Closing a file system will close all open {@link
     * java.nio.channels.Channel channels}, {@link DirectoryStream directory-streams},
     * {@link java.nio.file.WatchService watch-service}, and other closeable objects associated
     * with this file system.
     *
     * @throws IOException
     *          If an I/O error occurs
     */
    abstract protected void doCloseFileSystem()

    /** Check is filesystem is open.
     *
     * @throw {@link java.nio.file.ClosedFileSystemException} if not open
     */
    protected void checkOpen() {
        if(!open) {
            throw new ClosedFileSystemException()
        }
    }

    private final FileSystemProvider fileSystemProvider
    private boolean open = true
}
