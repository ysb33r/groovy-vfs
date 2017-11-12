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

    /** Splits out the user information but leave it encoded.
     *
     * @param uri URI potentially containing user info
     * @return User information containing either just a username or a username and password. Can be null
     * if no user information was present in the URI.
     */
    static String[] splitRawUserInfo(final URI uri) {
        uri.rawUserInfo?.split(':')
    }

    /** Splits the query part out into separate elements
     *
     * @param uri An instance of an URI that might have a query path.
     * @return A map of raw query parts. Can be empty, but never null
     */
    static Map<String,String> splitRawQuery(final URI uri) {
        Map<String,String> params = [:]
        uri.rawQuery.split('&').each { String rawPart ->
            String[] subparts = rawPart.split('=')
            params[ subparts[0] ] = subparts.size() > 1 ? subparts[1..-1].join('=') : ''
        }

        params
    }

    /** Splits the query part out into separate elements
     *
     * @param uri An instancr of an URI tha might have a query patr.
     * @return A map of decoded query parts. Can be empty, but never null
     */
    static Map<String,String> splitQuery(final URI uri) {
        Map<String,String> params = [:]
        splitRawQuery(uri).each { String key,String value ->
            params.put(UriUtils.decode(key),decode(value))
        }
        params
    }

    /** URI decode a string
     *
     * @param value Encoded string
     * @return Decoded URI string
     */
    static String decode(final String value) {
        URLDecoder.decode(value,'UTF-8')
    }

    /** URI decode a string
     *
     * @param value Encoded string
     * @return Decoded URI string
     */
    static String decode(final CharSequence value) {
        decode(value.toString())
    }

    /** URI decode a string
     *
     * @param value Encoded string
     * @return Decoded URI string
     */
    static String decode(final char[] value) {
        decode(value.toString())
    }

    /** Encodes a string suitable for URI query or fragment part.
     *
     * @param value URI query or fragment part to be encoded.
     * @return Encoded string part
     */
    static String encode(final String value) {
        URLEncoder.encode(value,'UTF-8')
    }

    /** Encodes a string suitable for URI query or fragment part.
     *
     * @param value URI query or fragment part to be encoded.
     * @return Encoded string part
     */
    static String encode(final CharSequence  value) {
        encode(value.toString())
    }

    /** Encodes a string suitable for URI query or fragment part.
     *
     * @param value URI query or fragment part to be encoded.
     * @return Encoded string part
     */
    static String encode(final char[] value) {
        encode(value.toString())
    }

}
