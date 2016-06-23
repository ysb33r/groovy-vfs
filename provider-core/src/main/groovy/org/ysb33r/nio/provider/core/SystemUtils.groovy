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

import groovy.transform.CompileStatic

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
class SystemUtils {

    /** Looks to find wehterh a provider scheme has been overriden by a property setting.
     *
     * @parma baseName A basename for provider i.e. {@code org.ysb33r.nio.provider}.
     * @param scheme Scheme to look for
     * @return New provider scheme name if found, otherwise whatever was passed as {@code scheme}.
     */
    static String getSchemeName(final String baseName, final String scheme) {
        System.getProperty("${baseName}.${scheme}.scheme") ?: scheme
    }

    /** Looks to find a property name and return it as an integer
     *
     * @param propertyName Property to find
     * @return Property converted to an integer or null if porety cannot be found or cannot be converted to Integer.
     */
    static Integer getIntegerProperty(final String propertyName) {
        try {
            return System.getProperty(propertyName)?.toInteger()
        } catch (NumberFormatException) {
            return null
        }
    }

}
