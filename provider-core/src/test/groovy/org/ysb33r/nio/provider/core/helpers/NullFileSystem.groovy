package org.ysb33r.nio.provider.core.helpers

import java.nio.file.FileStore
import java.nio.file.FileSystem
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.WatchService
import java.nio.file.attribute.UserPrincipalLookupService
import java.nio.file.spi.FileSystemProvider

/**
 * @author Schalk W. Cronjé
 */
class NullFileSystem extends FileSystem {
    @Override
    FileSystemProvider provider() {
        return null
    }

    @Override
    void close() throws IOException {

    }

    @Override
    boolean isOpen() {
        return false
    }

    @Override
    boolean isReadOnly() {
        return false
    }

    @Override
    String getSeparator() {
        '/'
    }

    @Override
    Iterable<Path> getRootDirectories() {
        return null
    }

    @Override
    Iterable<FileStore> getFileStores() {
        return null
    }

    @Override
    Set<String> supportedFileAttributeViews() {
        return null
    }

    @Override
    Path getPath(String first, String... more) {
        return null
    }

    @Override
    PathMatcher getPathMatcher(String syntaxAndPattern) {
        return null
    }

    @Override
    UserPrincipalLookupService getUserPrincipalLookupService() {
        return null
    }

    @Override
    WatchService newWatchService() throws IOException {
        return null
    }
}
