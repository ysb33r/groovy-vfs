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


class VfsXmlSpec extends Specification  {
    def vfsxml = """
<providers>
    <default-provider class-name="o.a.c.v.p.url.UrlFileProvider">
    </default-provider>
    <provider class-name="o.a.c.v.p.local.DefaultLocalFileProvider">
        <scheme name="file"/>
    </provider>
    <provider class-name="o.a.c.v.p.zip.ZipFileProvider">
        <scheme name="zip"/>
    </provider>
    <provider class-name="o.a.c.v.p.tar.TarFileProvider">
        <scheme name="tar"/>
        <if-available class-name="org.apache.commons.compress.archivers.tar.TarArchiveOutputStream"/>
    </provider>

    <provider class-name="o.a.c.v.p.bzip2.Bzip2FileProvider">
        <scheme name="bz2"/>
        <if-available class-name="org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream"/>
    </provider>
    <provider class-name="o.a.c.v.p.gzip.GzipFileProvider">
        <scheme name="gz"/>
    </provider>

    <provider class-name="o.a.c.v.p.jar.JarFileProvider">
        <scheme name="jar"/>
        <scheme name="sar"/>
        <scheme name="ear"/>
        <scheme name="par"/>
        <scheme name="ejb3"/>
        <scheme name="war"/>
    </provider>
    <provider class-name="o.a.c.v.p.temp.TemporaryFileProvider">
        <scheme name="tmp"/>
    </provider>
    <provider class-name="o.a.c.v.p.ftp.FtpFileProvider">
        <scheme name="ftp"/>
        <if-available class-name="org.apache.commons.net.ftp.FTPFile"/>
    </provider>
    <provider class-name="o.a.c.v.p.ftps.FtpsFileProvider">
        <scheme name="ftps"/>
        <if-available class-name="org.apache.commons.net.ftp.FTPFile"/>
    </provider>
    <provider class-name="o.a.c.v.p.http.HttpFileProvider">
        <scheme name="http"/>
        <if-available class-name="org.apache.commons.httpclient.HttpClient"/>
    </provider>
    <provider class-name="o.a.c.v.p.https.HttpsFileProvider">
        <scheme name="https"/>
        <if-available class-name="org.apache.commons.httpclient.HttpClient"/>
    </provider>
    <provider class-name="o.a.c.v.p.sftp.SftpFileProvider">
        <scheme name="sftp"/>
        <if-available class-name="javax.crypto.Cipher"/>
        <if-available class-name="com.jcraft.jsch.JSch"/>
    </provider>
    <provider class-name="o.a.c.v.p.res.ResourceFileProvider">
        <scheme name="res"/>
    </provider>
    <provider class-name="o.a.c.v.p.webdav.WebdavFileProvider">
        <scheme name="webdav"/>
        <if-available class-name="org.apache.commons.httpclient.HttpClient"/>
        <if-available class-name="org.apache.jackrabbit.webdav.client.methods.DavMethod"/>
    </provider>
    <provider class-name="o.a.c.v.p.tar.TarFileProvider">
        <scheme name="tgz"/>
        <if-available scheme="gz"/>
        <if-available scheme="tar"/>
    </provider>

    <operationProvider class-name="acl.AclOperationsProvider">
        <scheme name="s3" />
        <scheme name="aws" />
    </operationProvider>

    <extension-map extension="zip" scheme="zip"/>
    <extension-map extension="tar" scheme="tar"/>
    <mime-type-map mime-type="application/zip" scheme="zip"/>
    <mime-type-map mime-type="application/x-tar" scheme="tar"/>
    <mime-type-map mime-type="application/x-gzip" scheme="gz"/>
    <extension-map extension="jar" scheme="jar"/>
    <extension-map extension="bz2" scheme="bz2"/>
    <extension-map extension="gz" scheme="gz"/>
    <extension-map extension="tgz" scheme="tar"/>
    <extension-map extension="tbz2" scheme="tar"/>

</providers>
"""
    def "If no default-provider in XML then defaultProvider should be null"() {
        given:
            def ps = VfsXml.createProviderSpec( '<providers/>' )

        expect:
            ps.defaultProvider == null
    }

    def "If no class-name on provider, throw exception"() {
        when:
            def ps = VfsXml.createProviderSpec( '<providers><provider/></providers>' )

        then:
            thrown(org.ysb33r.groovy.dsl.vfs.MapParseException)
    }

    def "If no scheme on provider, throw exception"() {
        when:
        def ps = VfsXml.createProviderSpec( '<providers><provider class-name="a"/></providers>' )

        then:
        thrown(org.ysb33r.groovy.dsl.vfs.MapParseException)
    }

    def "Creating a ProviderSpecification from textual XML" () {
        given:
            def ps = VfsXml.createProviderSpec( vfsxml )

        expect:
            ps.defaultProvider.className == "o.a.c.v.p.url.UrlFileProvider"
            ps.providers.size() == 15
            ps.providers[2].className == "o.a.c.v.p.tar.TarFileProvider"
            ps.providers[2].schemes.size() == 1
            ps.providers[2].schemes[0] == 'tar'
            ps.providers[2].dependsOnSchemes.size() == 0
            ps.providers[2].dependsOnClasses.size() == 1
            ps.providers[2].dependsOnClasses[0] == 'org.apache.commons.compress.archivers.tar.TarArchiveOutputStream'
            ps.providers[5].schemes.size() == 6
            ps.providers[5].schemes[3] == 'par'
            ps.providers[11].dependsOnClasses.size() == 2
            ps.providers[11].dependsOnClasses[0] == 'javax.crypto.Cipher'
            ps.providers[11].dependsOnClasses[1] == 'com.jcraft.jsch.JSch'
            ps.providers[14].className == "o.a.c.v.p.tar.TarFileProvider"
            ps.providers[14].dependsOnSchemes.size() == 2
            ps.providers[14].dependsOnSchemes[0] == 'gz'
            ps.providers[14].dependsOnSchemes[1] == 'tar'
            ps.providers[14].dependsOnClasses.size() == 0
            ps.operationProviders.size() == 1
            ps.operationProviders[0].className == 'acl.AclOperationsProvider'
            ps.operationProviders[0].schemes.size() == 2
            ps.operationProviders[0].schemes[0] == 's3'
            ps.operationProviders[0].schemes[1] == 'aws'
            ps.mimeTypes.size() == 3
            ps.mimeTypes[1].key == 'application/x-tar'
            ps.mimeTypes[1].scheme == 'tar'
            ps.extensions.size() == 7
            ps.extensions[5].extension == 'tgz'
            ps.extensions[5].scheme == 'tar'
    }

    def "Must be able to process a providers.xml file from Apache VFS 2.0"() {
        given:
            def ps = VfsXml.createProviderSpec( new File("${System.getProperty('TESTFSREADROOT')}/src/test/resources/providers/vfs20-providers.xml") )

        expect:
            ps.defaultProvider.className == "org.apache.commons.vfs2.provider.url.UrlFileProvider"
            ps.providers.size() == 17
            ps.providers[2].schemes[0] == 'tar'
            ps.providers[2].dependsOnSchemes.size() == 0
            ps.providers[2].dependsOnClasses.size() == 1
            ps.providers[7].schemes[0] == 'ftp'

    }
}