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
import spock.lang.Stepwise


/**
 * Tests are run in order as we are fiddling with system properties.
 */
@Stepwise
class SystemUtilsSpec extends Specification {
    final static String TESTPROP_INT = 'org.ysb33r.nio.provider.core.SystemUtilsSpec.intvalue'
    final static String TESTPROP_SCHEME_BASE = 'org.ysb33r.nio.provider.core.SystemUtilsSpec'
    final static String SCHEME = 'SystemUtils'
    final static String TESTPROP_SCHEME = "${TESTPROP_SCHEME_BASE}.${SCHEME}.scheme"

    static String previousInt
    static String previousScheme

    void setupSpec() {
        String previousInt = System.getProperty(TESTPROP_INT)
        String previousScheme = System.getProperty(TESTPROP_SCHEME)
    }

    void cleanupSpec() {
        if(previousInt) {
            System.setProperty(TESTPROP_INT,previousInt)
        } else {
            System.clearProperty(TESTPROP_INT)

        }
        if(previousScheme) {
            System.setProperty(TESTPROP_SCHEME,previousScheme)
        } else {
            System.clearProperty(TESTPROP_SCHEME)
        }
    }

    def "Retrieving a scheme property that does not exist"() {
        given: "A scheme which is not set via system property"
        System.clearProperty(TESTPROP_SCHEME)

        expect: "The property will be the name of the scheme"
        SystemUtils.getSchemeName(TESTPROP_SCHEME_BASE,SCHEME) == SCHEME
    }

    def "Retrieving a scheme property that exists"() {
        given: "A scheme set to foobar"
        System.setProperty(TESTPROP_SCHEME,'foobar')

        expect: "The scheme name retirebed via system property will be foobar"
        SystemUtils.getSchemeName(TESTPROP_SCHEME_BASE,SCHEME) == 'foobar'
    }

    def "Converting a valid integer property"() {
        given: 'A system property of "1234"'
        System.setProperty(TESTPROP_INT,'1234')

        expect: 'It to be 1234'
        SystemUtils.getIntegerProperty(TESTPROP_INT) == 1234
    }

    def "Converting an invalid integer property"() {
        given: 'A system property of "abc1234"'
        System.setProperty(TESTPROP_INT,'abc1234')

        expect: 'It will be null'
        SystemUtils.getIntegerProperty(TESTPROP_INT) == null
    }

    def "Converting an non-existant integer property"() {
        given: 'A system property that is not set'
        System.clearProperty(TESTPROP_INT)

        expect: 'It will be null'
        SystemUtils.getIntegerProperty(TESTPROP_INT) == null
    }
}