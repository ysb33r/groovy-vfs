/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2015
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
// ============================================================================
// (C) Copyright Schalk W. Cronje 2014
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================

package org.ysb33r.groovy.dsl.vfs.impl


import spock.lang.*


class ProviderSpecificationSpec extends Specification  {

    def "Default ProviderSpec only has a default provider specified"() {
        given:
            def vfsps = new ProviderSpecification()

        expect:
            vfsps.defaultProvider.className == 'org.apache.commons.vfs2.provider.url.UrlFileProvider'
            vfsps.defaultProvider.schemes.size() == 0
    }

    def "A ProviderSpec must be constructable with a list of providers and the order must be maintained"() {
        given:
            def vfsps = new ProviderSpecification(
                    providers : [
                        new Provider( className : 'a.b.c.d', schemes : ['ab','cd'] ),
                        new Provider( className : 'e.f', schemes : ['ef'] ),
                    ]
            )

        expect:
            vfsps.providers.size() == 2
            vfsps.providers[0].className == 'a.b.c.d'
            vfsps.providers[1].schemes[0] == 'ef'

    }
}