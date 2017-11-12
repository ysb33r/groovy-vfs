package org.ysb33r.vfs.auth

import groovy.transform.CompileStatic

/** A collection of basic, probably useful, and potentially naive implementations of credentials.
 *
 * @since 2.0
 */
@CompileStatic
class Credentials {

    /** Creates a {@link BasicCredentials} instance from a username and a password.
     *
     * <p> The password is stored internally in a character array.
     *
     * @param user Username
     * @param pass Password
     * @return {@link BasicCredentials} instance.
     */
    static BasicCredentials fromUsernamePassword(final CharSequence user,final CharSequence pass) {
        new Basic(user,pass)
    }


    private static class Basic implements BasicCredentials {

        Basic(final CharSequence user,final CharSequence pass) {
            this.user = new String(user.toString())
            this.pass = pass?.toString()?.toCharArray()
        }

        @Override
        CharSequence getUsername() {
            this.user
        }

        @Override
        char[] getPassword() {
            this.pass
        }

        final private String user
        final private char[] pass
    }
}
