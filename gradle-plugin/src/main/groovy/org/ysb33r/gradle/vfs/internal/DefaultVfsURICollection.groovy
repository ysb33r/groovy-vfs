package org.ysb33r.gradle.vfs.internal

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.StopExecutionException
import org.ysb33r.gradle.vfs.VfsURI
import org.ysb33r.gradle.vfs.VfsURICollection

/**
 * Created by schalkc on 02/01/15.
 */
@CompileStatic
class DefaultVfsURICollection implements VfsURICollection {

    /** Checks whether a specific URI is within a collection
     *
     * @param uri
     * @return Return {@code true} is the uri is contained within the collection.
     */
    /** Adds a URI to this collection
     *
     * @param uri URI to be added
     * @return This updated collection
     */
    @Override
    VfsURICollection add(VfsURI uri) {
        uris+= uri
        this
    }

    /** Checks whether a specific URI is within a collection. The strings representations
     * are compared to determine whether presence. This allows for both resolved and taged URIs
     * to be compared. Any vfs query properties will be ignored.
     *
     * @param uri
     * @return Return {@code true} is the uri is contained within the collection.
     */
    @Override
    boolean contains(VfsURI uri) {
        String needle = uri.uri.toString()
        null != uris.find { VfsURI it ->
            it.uri.toString() == needle
        }
    }


    /** Check whether collection is empty
     *
     * @return Returns true if this collection is empty.
     */
    @Override
    boolean isEmpty() {
        uris.empty
    }

    /** Throws a StopExecutionException if this collection is empty.
     *
     * @return Return this collection if it is not empty
     */
    @Override
    VfsURICollection stopExecutionIfEmpty() {
        if(isEmpty()) {
            throw new StopExecutionException('VfsURICollection is empty')
        }
        this
    }

    /** Checks whether all URIs in the collection have been resolved.
     *
     * @return {@code True} is all are resolved.
     */
    @Override
    boolean allResolved() {
        !(uris.any { !it.resolved })
    }

    /** Attempt to have all URIs resolved. Throws if there is a failure.
     *
     * @return This collection with all URIs resolved
     * @throw StopExecutionException if a URI cannot be resolved.
     */
    @Override
    VfsURICollection resolve() {

        if(!allResolved()) {
            List<VfsURI> tmp = []
            uris.each {
                if(it.resolved) {
                    tmp+= it
                } else {
                    tmp+= it.resolve()
                }
            }
            uris= tmp
        }

        this
    }

    /** Contents of collection.
     *
     * @return Contents of this collection as a resolved set.
     */
    @Override
    Set<VfsURI> getUris() {
        if(!allResolved()) {
            resolve()
        }
        uris as Set<VfsURI>
    }

    /** Restricts the contents of this collection to those URIs which match the given criteria.
     * The filtered collection is live, so that it reflects any changes to this collection.
     * The given closure is passed the VfsURI as a parameter, and should return a boolean value.
     * @param filterClosure The closure to use to select the contents of the filtered collection
     * @return The filtered collection
     */
    @Override
    VfsURICollection filter(Closure filterClosure) {
        filter([ isSatisfiedBy : filterClosure ] as Spec<VfsURI>)
    }

    /** Restricts the contents of this collection to those files which match the given criteria.
     * The filtered collection is live, so that it reflects any changes to this collection.
     *
     * @param filterSpec The criteria to use to select the contents of the filtered collection.
     * @return The filtered collection.
     */
    @Override
    VfsURICollection filter(Spec<VfsURI> filterSpec) {
        FilteredVfsURICollection.create(this,filterSpec)
    }

    /** Returns a collection which contains the intersection of this collection and the given collection.
     * The returned collection is live, and tracks changes to both source collections.
     * You can call this method in your build script using the - operator.
     *
     * @param collection The other collection. Should not be null.
     * @return A new collection containing the intersection.
     */
    @Override
    VfsURICollection minus(VfsURICollection collection) {
        return null
    }

    /** Returns a collection which contains the union of this collection and the given collection.
     * The returned collection is live, and tracks changes to both source collections.
     * You can call this method in your build script using the + operator.
     *
     * @param collection The other collection. Should not be null.
     * @return A new collection containing the union.s
     */
    @Override
    VfsURICollection plus(VfsURICollection collection) {
        return null
    }

   /**
     * Returns an iterator over elements of type {@code ResolvedURI}.
     *
     * @return an Iterator.
     */
    @Override
    Iterator iterator() {
        uris.iterator()
    }

    /** Creates an empty collection
     *
     */
    DefaultVfsURICollection() {}

    @PackageScope List<VfsURI> uris = []
}
