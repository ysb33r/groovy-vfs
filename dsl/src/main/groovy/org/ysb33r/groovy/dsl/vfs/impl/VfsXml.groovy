//
// ============================================================================
// (C) Copyright Schalk W. Cronje 2013-2015
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================
//

package org.ysb33r.groovy.dsl.vfs.impl

import org.ysb33r.groovy.dsl.vfs.MapParseException
import groovy.transform.CompileStatic
import groovy.util.slurpersupport.GPathResult
import java.net.URL


class VfsXml {

    @CompileStatic
    static ProviderSpecification createProviderSpec( String xml ) {
        _create( new XmlSlurper().parseText(xml) )
    }

    @CompileStatic
    static ProviderSpecification createProviderSpec( File f ) {
        _create( new XmlSlurper().parse(f) )
    }

    @CompileStatic
    static ProviderSpecification createProviderSpec( URL url ) {
        _create( new XmlSlurper().parse( url.toURI().toString() ) )
    }

    private static ProviderSpecification _create( GPathResult xml ) {
        ProviderSpecification ps = new ProviderSpecification()

        if(xml.'default-provider'.size()) {
            ps.defaultProvider.className = xml.'default-provider'.@'class-name'
        } else {
            ps.defaultProvider = null
        }

        xml.provider.each { p ->

            if(!p.@'class-name' )  {
                throw new MapParseException("No @class-name on 'provider'")
            }

            Provider prv = new Provider( className : p.@'class-name')

            p.scheme.each { s ->
                if(!s.@'name' )  {
                    throw new MapParseException("Missing scheme@name on provider ('${prv.className}')")
                }
                prv.schemes << s.@name.toString()
            }

            if(prv.schemes.size() == 0 )  {
                throw new MapParseException("No 'scheme' elements on provider ('${prv.className}')")
            }

            p.'if-available'.each { ia ->
                if (ia.@'class-name'.size() ) {
                    prv.dependsOnClasses << ia.@'class-name'.toString()
                }

                if (ia.@'scheme'.size()) {
                    prv.dependsOnSchemes << ia.@'scheme'.toString()
                }
            }

            ps.providers << prv
        }

        xml.operationProvider.each { op ->
            if(!op.@'class-name' )  {
                throw new MapParseException("No @class-name on 'operationProvider'")
            }

            OperationProvider oprv = new OperationProvider( className : op.@'class-name')

            op.scheme.each { s ->
                if(!s.@'name' )  {
                    throw new MapParseException("Missing scheme@name on operationProvider ('${oprv.className}')")
                }
                oprv.schemes << s.@name.toString()
            }

            if(oprv.schemes.size() == 0 )  {
                throw new MapParseException("No 'scheme' elements on operationProvider ('${oprv.className}')")
            }

            ps.operationProviders << oprv
        }

        xml.'mime-type-map'.each { mtm ->
            if(!mtm.@'mime-type' || !mtm.@'scheme' )  {
                throw new MapParseException("Invalid 'mime-type-map' found")
            }

            ps.mimeTypes << new MimeType( key : mtm.@'mime-type'.toString() , scheme : mtm.@'scheme'.toString() )
        }

        xml.'extension-map'.each { em ->
            if(!em.@'extension' || !em.@'scheme' )  {
                throw new MapParseException("Invalid 'extension-map' found")
            }

            ps.extensions << new Extension( extension : em.@'extension'.toString() , scheme : em.@'scheme'.toString() )
        }

        return ps
    }
}