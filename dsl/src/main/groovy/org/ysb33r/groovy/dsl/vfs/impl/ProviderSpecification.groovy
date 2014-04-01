// ============================================================================
// Copyright (C) Schalk W. Cronje 2012 - 2014
//
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

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor
import groovy.util.XmlSlurper

@CompileStatic
@TupleConstructor
@EqualsAndHashCode
class ProviderSpecification {
    Provider defaultProvider = new Provider( className : 'org.apache.commons.vfs2.provider.url.UrlFileProvider' )
    List<Provider> providers = []
    List<OperationProvider> operationProviders = []
    List<MimeType> mimeTypes = []
    List<Extension> extensions = []


    static final ProviderSpecification DEFAULT_PROVIDERS = new ProviderSpecification (
        defaultProvider : new Provider( className : 'org.apache.commons.vfs2.provider.url.UrlFileProvider' ),
        providers : [
            new Provider(
                className : 'org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider',
                schemes : [ 'file']
            ),
            new Provider(
                    className : 'org.apache.commons.vfs2.provider.zip.ZipFileProvider',
                    schemes : [ 'zip']
            ),
            new Provider(
                    className : 'org.apache.commons.vfs2.provider.tar.TarFileProvider',
                    schemes : [ 'tar'],
                    dependsOnClasses : [ 'org.apache.commons.vfs2.provider.tar.TarInputStream' ]
// 2.1                    dependsOnClasses : [ 'org.apache.commons.compress.archivers.tar.TarArchiveOutputStream' ]
            ),
            new Provider(
                    className : 'org.apache.commons.vfs2.provider.bzip2.Bzip2FileProvider',
                    schemes : [ 'bz2'],
                    dependsOnClasses : [ 'org.apache.commons.vfs2.provider.bzip2.CBZip2InputStream' ]
// 2.1                    dependsOnClasses : [ 'org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream' ]
            ),
            new Provider(
                    className : 'org.apache.commons.vfs2.provider.gzip.GzipFileProvider',
                    schemes : [ 'gz']
            ),
            new Provider(
                    className : 'org.apache.commons.vfs2.provider.jar.JarFileProvider',
                    schemes : [ 'jar','sar','ear','par','ejb3','war']
            ),
            new Provider(
                    className : 'org.apache.commons.vfs2.provider.temp.TemporaryFileProvider',
                    schemes : [ 'tmp']
            ),
            new Provider(
                    className : 'org.apache.commons.vfs2.provider.ftp.FtpFileProvider',
                    schemes : [ 'ftp'],
                    dependsOnClasses : [ 'org.apache.commons.net.ftp.FTPFile' ]
            ),
            new Provider(
                    className : 'org.apache.commons.vfs2.provider.ftps.FtpsFileProvider',
                    schemes : [ 'ftps'],
                    dependsOnClasses : [ 'org.apache.commons.net.ftp.FTPFile' ]
            ),
            new Provider(
                    className : 'org.apache.commons.vfs2.provider.http.HttpFileProvider',
                    schemes : [ 'http'],
                    dependsOnClasses : [ 'org.apache.commons.httpclient.HttpClient' ]
            ),
            new Provider(
                    className : 'org.apache.commons.vfs2.provider.https.HttpsFileProvider',
                    schemes : [ 'https'],
                    dependsOnClasses : [ 'org.apache.commons.httpclient.HttpClient' ]
            ),
            new Provider(
                    className : 'org.apache.commons.vfs2.provider.sftp.SftpFileProvider',
                    schemes : [ 'sftp'],
                    dependsOnClasses : [ 'javax.crypto.Cipher','com.jcraft.jsch.JSch' ]
            ),
            new Provider(
                    className : 'org.apache.commons.vfs2.provider.res.ResourceFileProvider',
                    schemes : [ 'res']
            ),
            new Provider(
                    className : 'org.apache.commons.vfs2.provider.webdav.WebdavFileProvider',
                    schemes : [ 'webdav'],
                    dependsOnClasses : [
                        'org.apache.commons.httpclient.HttpClient',
                        'org.apache.jackrabbit.webdav.client.methods.DavMethod'
                    ]
            ),
            new Provider(
                    className : 'org.apache.commons.vfs2.provider.tar.TarFileProvider',
                    schemes : [ 'tgz'],
                    dependsOnSchemes : [ 'gz','tar' ]
            ),
            new Provider(
                    className : 'org.apache.commons.vfs2.provider.tar.TarFileProvider',
                    schemes : [ 'tbz2'],
                    dependsOnSchemes : [ 'bz2','tar' ]
            ),
            new Provider(
                    className : 'org.apache.commons.vfs2.provider.ram.RamFileProvider',
                    schemes : [ 'ram']
            ),
            new Provider(
                    className : 'org.apache.commons.vfs2.provider.hdfs.HdfsFileProvider',
                    schemes : [ 'hdfs'],
                    dependsOnClasses: [ 'org.apache.hadoop.fs.FileSystem' ]
            ),
            new Provider(
                    className : 'org.apache.commons.vfs2.provider.smb.SmbFileProvider',
                    schemes : [ 'smb','cifs'],
                    dependsOnClasses: [ 'jcifs.smb.SmbFile' ]
            ),
            new Provider(
                    className : 'org.apache.commons.vfs2.provider.mime.MimeFileProvider',
                    schemes : [ 'mime'],
                    dependsOnClasses: [ 'javax.mail.internet.MimeMultipart' ]
            ),
        ],
        mimeTypes : [
            new MimeType( key : 'application/zip',    scheme : 'zip' ),
            new MimeType( key : 'application/x-tar',  scheme : 'tar' ),
            new MimeType( key : 'application/x-gzip', scheme : 'gz' ),
            new MimeType( key : 'message/rfc822',     scheme : 'mime' )
        ],
        extensions : [
            new Extension( extension : 'zip',  scheme : 'zip'),
            new Extension( extension : 'tar',  scheme : 'tar'),
            new Extension( extension : 'jar',  scheme : 'jar'),
            new Extension( extension : 'bz2',  scheme : 'bz2'),
            new Extension( extension : 'gz',   scheme : 'gz'),
            new Extension( extension : 'tgz',  scheme : 'tar'),
            new Extension( extension : 'tbz2', scheme : 'tar'),
            new Extension( extension : 'mime', scheme : 'mime')
        ]
    )

}

//    <!--
//    <provider class-name="org.apache.commons.vfs2.provider.svn.SvnFileProvider">
//        <scheme name="svnhttps"/>
//    </provider>
//    -->
//    <!--
//    <mime-type-map mime-type="application/x-tgz" scheme="tgz"/>
//    -->
//    <!--
//    <extension-map extension="tgz" scheme="tgz"/>
//    <extension-map extension="tbz2" scheme="tbz2"/>
//    -->


//

