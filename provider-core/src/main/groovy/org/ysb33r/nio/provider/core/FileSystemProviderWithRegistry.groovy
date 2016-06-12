package org.ysb33r.nio.provider.core

import groovy.transform.CompileStatic

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
interface FileSystemProviderWithRegistry {

    /** Given a URI, extract a value suitable for use as a key in a filesystem registry
     *
     * @param uri URI to a filesystem
     * @return Key
     */
    String getKey(final URI uri)

    /** PRovides access to a filesystem registry
     *
     * @return Instance of {@link FileSystemRegistry}
     */
    FileSystemRegistry getFileSystemRegistry()

}