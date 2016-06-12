package org.ysb33r.nio.provider.core.helpers

import groovy.transform.CompileStatic

import java.nio.channels.SeekableByteChannel
import java.nio.file.AccessMode
import java.nio.file.CopyOption
import java.nio.file.DirectoryStream
import java.nio.file.FileStore
import java.nio.file.LinkOption
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.WatchService
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.FileAttributeView
import java.nio.file.attribute.UserPrincipalLookupService
import java.nio.file.spi.FileSystemProvider
import java.nio.file.FileSystem

/** A test FileSystemsProvider that return null for nearly everything. It's primary purpose
 * it to check that calls are made. It contains a map that is updated for every method call
 *
 * @author Schalk W. Cronjé
 */
@CompileStatic
class NullFileSystemProvider extends FileSystemProvider {

    final Map< String, List<String> > callsMade = [:]

    String getScheme() {
        called 'getScheme'
        'null'
    }

    @Override
    FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        called 'newFileSystem', uri.toString(), env.toString()
        FileSystemProvider fsp = this
        new FileSystem() {
            @Override
            FileSystemProvider provider() {
                fsp
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
                return null
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
    }

    @Override
    FileSystem getFileSystem(URI uri) {
        called 'getFileSystem',uri.toString()
    }

    @Override
    Path getPath(URI uri) {
        called 'getPath',uri.toString()
        null
    }

    @Override
    SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) {
        called 'newByteChannel',path.toString(),options.toString(),attrs.collect {it.toString()}.join(',')
        null
    }

    DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) {
        called 'newDirectoryStream',dir.toString(),filter.toString()
        return null
    }

    @Override
    void createDirectory(Path dir, FileAttribute<?>... attrs)  {
        called 'createDirectory',dir.toString(),attrs.collect {it.toString()}.join(',')
    }

    @Override
    void delete(Path path) {
        called 'delete',path.toString()
    }

    @Override
    void copy(Path source, Path target, CopyOption... options) {
        called 'copy',source.toString(),target.toString(),options.collect {it.toString()}.join(',')
    }

    @Override
    void move(Path source, Path target, CopyOption... options)  {
        called 'move',source.toString(),target.toString(),options.collect {it.toString()}.join(',')
    }

    @Override
    boolean isSameFile(Path path, Path path2) {
        called 'isSameFile', path.toString(),path2.toString()
        false
    }


    boolean isHidden(Path path) {
        called 'isHidden', path.toString()
        return false
    }

    @Override
    FileStore getFileStore(Path path) {
        called 'getFileStore', path.toString()
        return null
    }

    @Override
    void checkAccess(Path path, AccessMode... modes) throws IOException {
        called 'checkAccess',path.toString(),modes.collect {it.toString()}.join(',')
    }

    @Override
    def <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        called 'getFileAttributeView', path.toString(), type.toString(),  options.collect { it.toString() }.join(',')
        null
    }

    @Override
    def <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) {
        called 'readAttributes', path.toString(), type.toString(),  options.collect { it.toString() }.join(',')
        null
    }

    @Override
    Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options)  {
        called 'readAttributes', path.toString(), attributes,  options.collect { it.toString() }.join(',')
        [:]
    }

    @Override
    void setAttribute(Path path, String attribute, Object value, LinkOption... options)  {
        called 'setAttribute', path.toString(), attribute, value.toString(), options.collect { it.toString() }.join(',')
    }

    private void called(final String name,String... args) {
        callsMade[name] = args as List<String>
    }
}
