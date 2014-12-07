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
package org.ysb33r.groovy.vfs.app

import org.ysb33r.groovy.dsl.vfs.VFS
import spock.lang.Specification


/**
 * @author Schalk W. Cronjé.
 */
class ListSchemesSpec extends Specification {

    StringWriter errors
    StringWriter usage
    Cmdline cmdline

    void setup() {
        errors = new StringWriter()
        usage = new StringWriter()
        cmdline = new Cmdline(name: 'TEST_U', usageWriter: new PrintWriter(usage), errorWriter: new PrintWriter(errors))
    }

    def "Passing --lists-schems loads correct parser"() {
        given:
            def parser = cmdline.parse(['--list-schemes'] as String[])

        expect:
            !errors.toString().contains('Bad format')
            parser instanceof Cmd

    }
    def "Validate list of expected schemes"() {
        given:
            def cmd = cmdline.parse (['--list-schemes'] as String[])
            def runner = new Run([cmd] as List<Cmd>)
            runner.execute()

        expect:
            usage.toString().contains(scheme)

        where:
            scheme   || dummy
            'bz2'    || 0
            'ear'    || 0
            'ejb3'   || 0
            'file'   || 0
            'ftp'    || 0
            'ftps'   || 0
            'gz'     || 0
            'http'   || 0
            'https'  || 0
            'jar'    || 0
            'par'    || 0
            'ram'    || 0
            'res'    || 0
            'sar'    || 0
            'sftp'   || 0
            'tar'    || 0
            'tbz2'   || 0
            'tgz'    || 0
            'tmp'    || 0
            'war'    || 0
            'zip'    || 0
            'cifs'   || 0
            'smb'    || 0
            's3'     || 0

    }
}