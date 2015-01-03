package org.ysb33r.gradle.vfs.internal

import groovy.transform.CompileStatic
import org.ysb33r.gradle.vfs.VfsURI
import org.ysb33r.gradle.vfs.VfsURICollection
import org.ysb33r.groovy.dsl.vfs.VFS

/** Collection of utilities to work with resolved URIs.
 *
 * @since 1.0
 * @author Schalk W. Cronjé
 */
@CompileStatic
class UriUtils {

    /** Takes a URI and stages it in the context of a VFS. All supplied options will applied as far as
     * possible. Non-VFS options will be added as praxis options.
     *
     * @param opts Vfs and praxis options
     * @param vfs The VFS to stage against.
     * @param uri The URI to stage
     * @return A resolved URI with praxis options
     */
    static VfsURI uriWithOptions( Map<String,Object> opts, VFS vfs, Object uri) {
        StagedURI.create(opts,vfs,uri)
    }

    /** Takes a list of URIs and stages them in the context of a VFS. All supplied options will applied as far as
     * possible. Non-VFS options will be added as praxis options.
     *
     * @param opts Vfs and praxis options
     * @param vfs The VFS to stage against.
     * @param uri List of URIs to stage
     * @return List of resolved URIs
     */
    static VfsURICollection uriWithOptions( Map<String,Object> opts, VFS vfs, List<Object> uris) {
        VfsURICollection collection = emptyURICollection()

        uris.each {
            collection.add(uriWithOptions(opts,vfs,it))
        }

        collection
    }

    /** Creates a live filtered list which only contain local file URIs
     *
     * @param uris Original list
     * @return Filtered list
     */
    static VfsURICollection localURIs( VfsURICollection uris) {
        uris.filter { VfsURI uri -> uri.isLocal() }
    }

    /** Creates a live filtered list which do not contain any local file URIs
     *
     * @param uris Original list
     * @return Filtered list
     */
    static VfsURICollection remoteURIs( VfsURICollection uris) {
        uris.filter { VfsURI uri -> !uri.isLocal() }
    }

    /** Returns an empty ResolvedURICollection
     *
     * @return ResolvedURICollection instance with no members
     */
    static VfsURICollection emptyURICollection() {
        new DefaultVfsURICollection()
    }
}
