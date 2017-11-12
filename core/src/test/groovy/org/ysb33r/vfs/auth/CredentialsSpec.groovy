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

import spock.lang.Specification

import java.security.InvalidParameterException

class CredentialsSpec extends Specification {

    def "Create basic authentication supplier withj username & password"() {
        given:
        BasicCredentials creds = Credentials.fromUsernamePassword('foo','bar')

        expect:
        creds.username == 'foo'
        creds.password.toString() == 'bar'
    }

    def "Create basic authentication supplier with username only"() {
        given:
        BasicCredentials creds = Credentials.fromUsernamePassword('foo',null)

        expect:
        creds.username == 'foo'
        creds.password == null
    }

    def "Username cannot be null for a basic authentication supplier"() {
        when:
        BasicCredentials creds = Credentials.fromUsernamePassword(null,null)

        then:
        thrown(InvalidParameterException)
    }
}