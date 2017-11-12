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

    /** Resolves a new URI relative to a root URI.
     *
     * @param root Root URI
     * @param relativePath Path relative to root
     * @return New URI.
     * @throw {@link URIException} is path cannot be resolved.
     *
     */
    VfsURI resolveURI(final VfsURI root,final CharSequence relativePath) {
        null
    }

    boolean isFile(final VfsURI uri) {
        false
    }

    boolean isFolder(final VfsURI uri) {
        false
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

//    /** Deletes files according to a selection criteria
//     *
//     * @param uri
//     * @param confirmation A predicate that should return {@code true} if a file must be deleted.
//     * @param selector Selects files for deletion. Can be null in which case it will be the equivalent of
//     *   {@link Selectors.SELECT_SELF} if {@code uri} is a file or {@link Selector.FILES_ONLY} if {@code }
//     */
//    void rm(final VfsURI uri, final Predicate<VfsURI> confirmation, final FileDeletionSelector selector) {
//        throw new FileSystemException("rm() NEEDS IMPLEMENTATION")
//    }

    // cp
    // mv
    // cat
    // mkdir
    // append
    // overwrite
    // mtime
    // local
    // exists
    // canListFolder

    private final ClassLoader classLoader
    private final FileSystemOptions defaultFSOptions = new FileSystemOptions()

}
