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

package org.ysb33r.groovy.dsl.vfs


import spock.lang.*


class ProviderSchemaSpec extends Specification  {

    def vfs = new VFS( ignoreDefaultProviders : true )

    def "Overriding defaultProvider should set new FileProvider in place"() {

        when:
            vfs {
                extend {
                    defaultProvider 'org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider'
                }
            }

        then:
            !vfs.fsMgr.hasProvider('file')
            !vfs.fsMgr.hasProvider('zip')
            vfs.fsMgr.resolveFile('ftp://build.gradle')

    }

    def "Supplying a provider spec, should add the provider"() {
        when:
            vfs {
                extend {
                    provider className : 'org.apache.commons.vfs2.provider.gzip.GzipFileProvider',schemes : ['gz']

                    provider className : 'org.apache.commons.vfs2.provider.tar.TarFileProvider',
                            schemes : ['tar'],
 // 2.0                           dependsOnClasses : ['org.apache.commons.vfs2.provider.tar.TarInputStream']
                            dependsOnClasses : ['org.apache.commons.compress.archivers.tar.TarArchiveOutputStream']

                    provider className : 'org.apache.commons.vfs2.provider.tar.TarFileProvider',
                            schemes : ['tgz'],
                            dependsOnSchemes : ['tar','gz']
                }
            }

        then:
            !vfs.fsMgr.hasProvider('file')
            vfs.fsMgr.hasProvider('gz')
            vfs.fsMgr.hasProvider('tar')
            vfs.fsMgr.hasProvider('tgz')

    }

    @Ignore
    def "Supplying an operation spec, should add the operation provider to an existing scheme"() {

    }

    def "Supplying a mime type spec, should add the mime type"() {
        when:
            vfs {
                extend {
                    provider className : 'org.apache.commons.vfs2.provider.zip.ZipFileProvider', schemes : ['zip']

                    mimeType 'application/zip',  'zip'
                }
            }

        then:
            vfs.fsMgr.hasProvider('zip')
    }

    def "Supplying a extension spec, should add the extension type"() {
        when:
        vfs {
            extend {
                provider className : 'org.apache.commons.vfs2.provider.zip.ZipFileProvider', schemes : ['zip']

                ext 'zip', 'zip'
            }
        }

        then:
        vfs.fsMgr.hasProvider('zip')
    }
}