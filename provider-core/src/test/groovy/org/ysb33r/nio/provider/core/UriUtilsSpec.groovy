/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2016
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
package org.ysb33r.nio.provider.core

import spock.lang.Specification


/**
 * @author Schalk W. Cronjé
 */
class UriUtilsSpec extends Specification {

    def "A URI should mask the password when printed"() {
        given: "A URI containing a password"
        URI uri = "http://user:password@foo/bar".toURI()

        expect: "The password to be replaced"
        UriUtils.friendlyURI(uri) == 'http://user:*****@foo/bar'
    }

    def "A URI with only a username does not add masking characters"() {
        given: "A URI containing a password"
        URI uri = "http://user@foo/bar".toURI()

        expect: "The password to be replaced"
        UriUtils.friendlyURI(uri) == 'http://user@foo/bar'
    }

    def "A URI with no credentials does not add masking characters"() {
        given: "A URI containing a password"
        URI uri = "http://foo/bar".toURI()

        expect: "The password to be replaced"
        UriUtils.friendlyURI(uri) == 'http://foo/bar'
    }
}