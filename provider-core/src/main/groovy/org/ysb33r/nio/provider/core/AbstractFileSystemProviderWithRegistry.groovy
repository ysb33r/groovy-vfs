package org.ysb33r.nio.provider.core

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.nio.file.FileAlreadyExistsException
import java.nio.file.FileSystem
import java.nio.file.spi.FileSystemProvider

/** Provides an abstract {@code FilesSystemProvider} which maintains an internal filesystem registry.
 *
 */
@CompileStatic
abstract class AbstractFileSystemProviderWithRegistry extends AbstractFileSystemProvider implements FileSystemProviderWithRegistry {

    final String scheme
    final FileSystemRegistry  fileSystemRegistry

    AbstractFileSystemProviderWithRegistry(final String scheme,FileSystemRegistry registry) {
        super(scheme)
        fileSystemRegistry = registry
    }


    /** Extract a suitable key from a URI that can be passed to a registry.
     *
     * @param uri
     */
    @Override
    String getKey(final URI uri) {
        (uri.port>0) ? "${uri.host}:${uri.port}" : uri.host
    }


    /**
     * Constructs a new {@code FileSystem} object identified by a URI. This
     * method is invoked by the {@link FileSystems#newFileSystem(URI, Map)}
     * method to open a new file system identified by a URI.
     *
     * <p> The {@code uri} parameter is an absolute, hierarchical URI, with a
     * scheme equal (without regard to case) to the scheme supported by this
     * provider. The exact form of the URI is highly provider dependent. The
     * {@code env} parameter is a map of provider specific properties to configure
     * the file system.
     *
     * <p> This method throws {@link FileSystemAlreadyExistsException} if the
     * file system already exists because it was previously created by an
     * invocation of this method. Once a file system is {@link
     * java.nio.file.FileSystem # close closed} it is provider-dependent if the
     * provider allows a new file system to be created with the same URI as a
     * file system it previously created.
     *
     * @param uri
     *          URI reference
     * @param env
     *          A map of provider specific properties to configure the file system;
     *          may be empty
     *
     * @return A new file system
     *
     * @throws IllegalArgumentException
     *          If the pre-conditions for the {@code uri} parameter aren't met,
     *          or the {@code env} parameter does not contain properties required
     *          by the provider, or a property value is invalid
     * @throws IOException
     *          An I/O error occurs creating the file system
     * @throws SecurityException
     *          If a security manager is installed and it denies an unspecified
     *          permission required by the file system provider implementation
     * @throws FileSystemAlreadyExistsException
     *          If the file system has already been created
     */
    @Override
    FileSystem newFileSystem(URI uri, Map<String, ?> env)  {
        validateURI(uri)
        String key = getKey(uri)
        if(fileSystemRegistry.contains(key)) {
            throw new FileAlreadyExistsException("Filesystem for '${uri}' already exists")
        }
        FileSystem newFs = doCreateFileSystem(uri,env)
        fileSystemRegistry.add(key,newFs)
        return newFs
    }


    /**
     * Returns an existing {@code FileSystem} created by this provider.
     *
     * <p> This method returns a reference to a {@code FileSystem} that was
     * created by invoking the {@link #newFileSystem(URI, Map) newFileSystem(URI,Map)}
     * method. File systems created the {@link #newFileSystem(java.nio.file.Path, Map)
     * newFileSystem(Path,Map)} method are not returned by this method.
     * The file system is identified by its {@code URI}. Its exact form
     * is highly provider dependent. In the case of the default provider the URI's
     * path component is {@code "/"} and the authority, query and fragment components
     * are undefined (Undefined components are represented by {@code null}).
     *
     * <p> Once a file system created by this provider is {@link
     * java.nio.file.FileSystem # close closed} it is provider-dependent if this
     * method returns a reference to the closed file system or throws {@code
     * FileSystemNotFoundException}. If the provider allows a new file system to
     * be created with the same URI as a file system it previously created then
     * this method throws the exception if invoked after the file system is
     * closed (and before a new instance is created by the {@link #newFileSystem
     * newFileSystem} method).
     *
     * <p> If a security manager is installed then a provider implementation
     * may require to check a permission before returning a reference to an
     * existing file system. In the case of the {@link FileSystems#getDefault
     * default} file system, no permission check is required.
     *
     * @param uri
     *          URI reference
     *
     * @return The file system
     *
     * @throws {@code IllegalArgumentException}
     *          If the pre-conditions for the {@code uri} parameter aren't met
     * @throws {@code FileSystemNotFoundException}
     *          If the file system does not exist
     * @throws {@code SecurityException}
     *          If a security manager is installed and it denies an unspecified
     *          permission.
     */
    @Override
    FileSystem getFileSystem(final URI uri) {
        validateURI(uri)
        String key = getKey(uri)
        if(!fileSystemRegistry.contains(key)) {
            throw new FileNotFoundException("Filesystem for key '${uri}' does not exist.")
        }
        fileSystemRegistry.get(key)
    }

    /** Returns the section out of a URI that will represent a path on this filesystem
     *
     * @param uri
     * @return
     */
    protected String extractFilePath(final URI uri) {
        uri.path
    }

    /**
     * Constructs a new {@code FileSystem} object identified by a URI. This
     * method is invoked by the {@link FileSystems#newFileSystem(URI, Map)}
     * method to open a new file system identified by a URI.
     *
     * <p> The {@code uri} parameter is an absolute, hierarchical URI, with a
     * scheme equal (without regard to case) to the scheme supported by this
     * provider. The exact form of the URI is highly provider dependent. The
     * {@code env} parameter is a map of provider specific properties to configure
     * the file system.
     *
     * <p> This method throws {@link FileSystemAlreadyExistsException} if the
     * file system already exists because it was previously created by an
     * invocation of this method. Once a file system is {@link
     * java.nio.file.FileSystem # close closed} it is provider-dependent if the
     * provider allows a new file system to be created with the same URI as a
     * file system it previously created.
     *
     * @param uri
     *          URI reference
     * @param env
     *          A map of provider specific properties to configure the file system;
     *          may be empty
     *
     * @return A new file system
     *
     * @throws {@code IllegalArgumentException}
     *          If the pre-conditions for the {@code uri} parameter aren't met,
     *          or the {@code env} parameter does not contain properties required
     *          by the provider, or a property value is invalid
     * @throws {@code IOException}
     *          An I/O error occurs creating the file system
     * @throws {@code SecurityException}
     *          If a security manager is installed and it denies an unspecified
     *          permission required by the file system provider implementation
     * @throws {@code FileSystemAlreadyExistsException}
     *          If the file system has already been created
     */
    protected abstract FileSystem doCreateFileSystem(URI uri,Map<String, ?> env)


}
