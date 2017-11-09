package org.ysb33r.vfs.core

import groovy.transform.CompileStatic

import java.util.function.Function
import java.util.function.Predicate

/**
 * @since 2.0
 */
@CompileStatic
class VfsEngine {

    VfsEngine() {
        this.classLoader = this.class.classLoader
    }

    VfsEngine(final ClassLoader loader) {
        this.classLoader = loader
    }

    FileSystemOptions getFileSystemOptions() {
        this.defaultFSOptions
    }

    /** Lists the contents of a URI.
     *
     * <p> If the URI points to a file, only the file is returned. If the URI points to a folder all of the direct
     * children of the fodler is returned,
     *
     *
     * @param uri URI to list.
     * @param apply Apply this predicate function to every item that is found.
     *   If the method returns {@code false} the listing is stopped.
     * @param selector Rules for selecting files and folders. Can be {@code null} which means select all children files and do not descend
     * into child folders.
     */
    void ls(final VfsURI uri, final Predicate<VfsURI> apply, final FileSelector selector ) {
        FileSelector selectorToUse = selector ?: Selectors.CHILDREN_ONLY
        throw new FileSystemException("ls() NEEDS IMPLEMENTATION")
    }

    /** Returns a list of URIs given a starting point in a filesystem
     *
     * <p> This method has the potential to return huge lists depending on the filesystem. Use with care.
     *
     * @param uri URI to list.
     * @param selector Rules for selecting files and folders. Can be {@code null} which means select all children files and do not descend
     * into child folders.
     * @return Iterable container of found folders and files. Exact type is implementation-dependent.
     */
    Iterable<VfsURI> ls(final VfsURI uri,final FileSelector selector) {
        Set<VfsURI> foundURIs = []

        Predicate<VfsURI> addItemsToContainer = { VfsURI encountered ->
            foundURIs.add(encountered)
            true
        } as Predicate<VfsURI>

        ls(uri,addItemsToContainer,selector)
        foundURIs
    }

    private final ClassLoader classLoader
    private final FileSystemOptions defaultFSOptions = new FileSystemOptions()

}
