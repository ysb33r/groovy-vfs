package org.ysb33r.gradle.vfs

/** Abstraction to work with resolved VFS URIs.
 *
 * @author Schalk W. Cronjé
 */
interface VfsURI {

    /** URI that has been resolved
     */
    Object getUri()

    /** Any VFS action options (praxis) that are associated with this URI
     *
     * @return List of potential options
     */
    Map<String,Object> getPraxis()

    /** Whether this URI is local
     *
     * @return {@code True} if local
     */
    boolean isLocal()

    /** Returns true is this URI has been resolved
     *
     * @return {@code True} if located on the virtual file system
     */
    boolean isResolved()

    /** Attempts to stage the URI if it is not already resolved,
     *
     * @return A resolved URI
     * @throw Exception if URI cannot be resolved
     */
    VfsURI resolve()
}