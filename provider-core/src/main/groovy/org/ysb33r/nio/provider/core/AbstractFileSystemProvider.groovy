package org.ysb33r.nio.provider.core

import groovy.transform.CompileStatic

import java.nio.file.FileAlreadyExistsException
import java.nio.file.FileSystem
import java.nio.file.spi.FileSystemProvider

/** Provides a base for a {@code FileSystemProvider}
 *
 */
@CompileStatic
abstract class AbstractFileSystemProvider extends FileSystemProvider {

    final String scheme

    AbstractFileSystemProvider(final String scheme) {
        this.scheme = System.getProperty(getSchemeSystemPropertyName(scheme)) ?: scheme
    }

    /** Returns the section out of a URI that will represent a path on this filesystem
     *
     * @param uri
     * @return
     */
    protected String extractFilePath(final URI uri) {
        uri.path
    }

    /** Validates the URI whether it is valid within the context of this provider
     *
     * @param uri
     * @throw {@code IllegalArgumentException} if URI is not valid.
     */
    protected void validateURI(final URI uri) {
        if(uri.isOpaque()) {
            throw new IllegalArgumentException("Opaque URIs (${uri.scheme} not followed by //) are not allowed in this context.")
        }
        if(uri.scheme != scheme) {
            throw new IllegalArgumentException("Provided scheme '${uri.scheme}' is invalid in this context. Expected '${scheme}'.")
        }
    }

    /** Returns a property name which can be used to override the default scheme name.
     *
     * @param scheme Default name of scheme
     * @return System property name
     */
    protected String getSchemeSystemPropertyName(final String scheme) {
        "org.ysb33r.nio.provider.${scheme}.scheme"
    }
}
