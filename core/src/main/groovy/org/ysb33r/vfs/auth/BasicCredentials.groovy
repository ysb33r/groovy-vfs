package org.ysb33r.vfs.auth

import groovy.transform.CompileStatic

/** Basic credentials based upon username and password.
 *
 * @since 2.0
 */
@CompileStatic
interface BasicCredentials extends CredentialsSupplier {

    /** Provides username
     *
     * @return Username as a sequence of characters. Never null.
     */
    CharSequence getUsername()

    /** Provides password.
     *
     * @return Password as a character array. Can be null if no password is required.
     */
    char[] getPassword()
}