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
import org.apache.commons.logging.impl.SimpleLog

class StandardFileSystemManagerSpec extends Specification {

    @Shared def simpleLog

    def setupSpec() {
        simpleLog = new SimpleLog(this.class.name)
        simpleLog.setLevel ( SimpleLog.LOG_LEVEL_ALL )
    }

    def "Using default c-tor should should be a null logger" () {

        given:
            def fsMgr = new StandardFileSystemManager()

        when:
            fsMgr.init()

        then:
            fsMgr.loggerInstance() instanceof org.apache.commons.logging.impl.NoOpLog
    }

    def "Loading one new scheme for valid class with no external deps should add the provider"() {

        given:
            def fsMgr = new StandardFileSystemManager()
            def provider = new Provider( className : 'org.apache.commons.vfs2.provider.tar.TarFileProvider', schemes : ['tar','foo'] )

        when:
            fsMgr.addProvider( provider )

        then:
            fsMgr.hasProvider( 'foo' )
            fsMgr.hasProvider( 'tar' )
    }

    def "Loading one new scheme for invalid class with no external deps should not add the provider"() {

        given:
            def fsMgr = new StandardFileSystemManager()
            def provider = new Provider( className : 'non.existing.class', schemes : ['tar','foo'] )

        when:
            fsMgr.addProvider( provider )

        then:
            !fsMgr.hasProvider( 'foo' )
            !fsMgr.hasProvider( 'tar' )
    }

    def "Loading one new scheme for valid class with valid external deps should add the provider"() {

        given:
            def fsMgr = new StandardFileSystemManager()
            def provider = new Provider(
                    className : 'org.apache.commons.vfs2.provider.ftp.FtpFileProvider',
                    schemes : ['ftp','foo'],
                    dependsOnClasses : [ 'org.apache.commons.net.ftp.FTPFile' ]
            )

        when:
            def result = fsMgr.addProvider( provider )

        then:
            result == true
            fsMgr.hasProvider( 'foo' )
            fsMgr.hasProvider( 'ftp' )
    }

    def "Loading tar scheme"() {

        given:
        def fsMgr = new StandardFileSystemManager()
        def provider = new Provider(
                className : 'org.apache.commons.vfs2.provider.tar.TarFileProvider',
                schemes : [ 'tar'],
                dependsOnClasses : [ 'org.apache.commons.vfs2.provider.tar.TarInputStream' ]
        )

        when:
        def result = fsMgr.addProvider( provider )

        then:
        result == true
        fsMgr.hasProvider( 'tar' )
    }



    def "Attempting to add new scheme for valid class with non-existing external deps should not add the provider"() {

        given:
            def fsMgr = new StandardFileSystemManager()
            def provider = new Provider(
                    className : 'org.apache.commons.vfs2.provider.ftp.FtpFileProvider',
                    schemes : ['ftp','foo'],
                    dependsOnClasses : [ 'non.existing.class.2' ]
            )

        when:
            def result = fsMgr.addProvider( provider )

        then:
            result == false
            !fsMgr.hasProvider( 'foo' )
            !fsMgr.hasProvider( 'ftp' )
    }

    def "Loading one new scheme for valid class which depends on existing scheme should add the provider"() {

        given:
            def fsMgr = new StandardFileSystemManager()
            def provider1 = new Provider( className : 'org.apache.commons.vfs2.provider.tar.TarFileProvider', schemes : ['tar','foo'] )
            def provider2 = new Provider(
                className : 'org.apache.commons.vfs2.provider.tar.TarFileProvider',
                schemes : ['tgz'],
                dependsOnSchemes : [ 'tar' ]
             )

        when:
            fsMgr.addProvider( provider1 )
            fsMgr.addProvider( provider2 )

        then:
            fsMgr.hasProvider( 'foo' )
            fsMgr.hasProvider( 'tar' )
            fsMgr.hasProvider( 'tgz' )
    }

    def "Loading one new scheme for valid class which depends on non-existing scheme should not add the provider"() {

        given:
            def fsMgr = new StandardFileSystemManager()
            def provider1 = new Provider( className : 'org.apache.commons.vfs2.provider.tar.TarFileProvider', schemes : ['tar','foo'] )
            def provider2 = new Provider(
                    className : 'org.apache.commons.vfs2.provider.tar.TarFileProvider',
                    schemes : ['tgz'],
                    dependsOnSchemes : [ 'tar2' ]
            )

        when:
            fsMgr.addProvider( provider1 )
            fsMgr.addProvider( provider2 )

        then:
            fsMgr.hasProvider( 'foo' )
            fsMgr.hasProvider( 'tar' )
            !fsMgr.hasProvider( 'tgz' )
    }

    @Ignore
    def "Attempting to add an operation provider with a valid class to existing schema should succeed"() {
        given:
            def fsMgr = new StandardFileSystemManager()
            def provider = new Provider( className : 'org.apache.commons.vfs2.provider.tar.TarFileProvider', schemes : ['tar','foo'] )
            def operation = new OperationProvider( className : 'non-implementing.class', schemes : ['tar'])

        when:
            fsMgr.addProvider( provider )
            fsMgr.addOperationProvider( operation )

        then:
            false
    }

    def "If a Apache VFS providers.xml file is found in the classpath, process it"() {
        given:
            def fsMgr = new StandardFileSystemManager()

        when:
            fsMgr.setLogger(simpleLog)
            fsMgr._configurePlugins( fsMgr.PLUGIN_DEFAULT_RESOURCE )

        then:
            fsMgr.hasProvider( 'ftp' )

    }

    def "If legacy mode specified, load using Apache VFS providers.xml"() {
        given:
            def fsMgr = new StandardFileSystemManager()

        when:
            fsMgr.setLogger(simpleLog)
            fsMgr.init(new ProviderSpecification(),true,true)

        then:
            fsMgr.hasProvider( 'ftp' )

    }
}
