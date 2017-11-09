package org.ysb33r.vfs.core

import groovy.transform.CompileStatic

import java.nio.file.Path

/** Utilities for dealing with URIs within a VFS context.
 *
 * @since 2.0
 */
@CompileStatic
class UriUtils {

    /** A string-presentation of a URI where the password is masked out.
     *
     * @param p {@link java.nio.file.Path} instance
     * @return A URI that can be safely printed or logged.
     */
    static String friendlyURI(final Path p) {
        friendlyURI(p.toUri())
    }

    /** A string-presentation of a URI where the password is masked out.
     *
     * @param uri Instance of a URI that needs to be printed or logged.
     * @return A URI that can be safely printed or logged.
     */
    static String friendlyURI(final URI uri) {
        if(uri.userInfo?.size()) {
            String[] parts = uri.userInfo.split(':',2)
            if(parts.size() ==1) {
                new URI(uri.scheme,parts[0],uri.host,uri.port,uri.path,uri.query,uri.fragment).toString()
            } else {
                new URI(uri.scheme,"${parts[0]}:*****",uri.host,uri.port,uri.path,uri.query,uri.fragment).toString()
            }
        } else {
            uri.toString()
        }
    }
}
