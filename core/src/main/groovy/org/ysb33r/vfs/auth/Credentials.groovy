/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2017
 *
 * This software is licensed under the Apache License 2.0
 * See http://www.apache.org/licenses/LICENSE-2.0 for license details
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 * ============================================================================
 */
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
