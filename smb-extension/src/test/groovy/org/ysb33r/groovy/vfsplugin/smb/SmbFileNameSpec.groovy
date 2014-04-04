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
package org.ysb33r.groovy.vfsplugin.smb

import spock.lang.*
import org.apache.commons.vfs2.FileType

class SmbFileNameSpec extends Specification {
    def "Constructing an SmbFileName should set all appropriate properties"() {
        given:
            def sfn = new SmbFileName(
                'foo',
                'localhost',
                1139,
                'ysb33r',
                'ThereIsNoPassword',
                'TEST',
                'ShareAndShareAlike',
                '/Foo/Bar',
                FileType.IMAGINARY
            )

        expect:
            sfn.baseName        == 'Bar'
            sfn.parent.baseName == 'Foo'
            sfn.path            == '/Foo/Bar'
            sfn.scheme          == 'foo'
            sfn.rootURI         == 'foo://TEST\\ysb33r:ThereIsNoPassword@localhost:1139/ShareAndShareAlike/'
    }
}