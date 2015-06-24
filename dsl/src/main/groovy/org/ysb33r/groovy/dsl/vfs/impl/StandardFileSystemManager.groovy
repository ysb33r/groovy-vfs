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

import groovy.transform.*
import org.ysb33r.groovy.dsl.vfs.FileSystemException
import org.ysb33r.groovy.dsl.vfs.MapParseException

import org.apache.commons.vfs2.provider.FileReplicator
import org.apache.commons.vfs2.provider.TemporaryFileStore
import org.apache.commons.vfs2.impl.DefaultFileSystemManager
import org.apache.commons.logging.Log
import org.apache.commons.logging.impl.NoOpLog
import org.apache.commons.vfs2.provider.FileProvider
import org.apache.commons.vfs2.operations.FileOperationProvider
import org.apache.commons.vfs2.impl.DefaultFileReplicator
import org.apache.commons.vfs2.impl.PrivilegedFileReplicator
import org.apache.commons.vfs2.CacheStrategy
import org.apache.commons.vfs2.FilesCache
import java.util.Enumeration
import java.net.URL
import java.io.IOException

/**
 * A {@link org.apache.commons.vfs2.FileSystemManager} that configures itself
 * from an XML (Default: providers.xml) configuration file.<br>
 * Certain providers are only loaded and available if the dependent library is in your
 * classpath. You have to configure your debugging facility to log "debug" messages to see
 * if a provider was skipped due to "unresolved externals".
 */
@CompileStatic
class StandardFileSystemManager extends DefaultFileSystemManager
{

    private static final String PLUGIN_CONFIG_RESOURCE = "META-INF/vfs-providers.xml"
    private static final String PLUGIN_DEFAULT_RESOURCE = "org/apache/commons/vfs2/impl/providers.xml"
//    private ClassLoader classLoader;

    StandardFileSystemManager() {
        super()
    }

    @CompileStatic
    Log loggerInstance() {return getLogger()}

//    /**
//     * Sets the ClassLoader to use to load the providers.  Default is to
//     * use the ClassLoader that loaded this class.
//     * @param classLoader The ClassLoader.
//     */
//    public void setClassLoader(final ClassLoader classLoader)
//    {
//        this.classLoader = classLoader;
//    }
//
    /**
     * Initializes this manager from a supplied provider specification.  Adds the providers and replicator.
     * @param providerSpec Specification for loading plugins
     * @param tfs TemporaryFileStore to use
     * @param replicator Override default replicator
     * @param vfslogger Logger instance to use
     * @param cacheStrategy Override defailt cache strategy
     * @param filesCache
     * @throws FileSystemException if an error occurs.
     */
    void init(
        ProviderSpecification providerSpec,
        boolean legacyMode,
        boolean scanForVfsXml,
        TemporaryFileStore tfs = null,
        FileReplicator replicator = null ,
        Log vfslogger = new NoOpLog(),
        org.apache.commons.vfs2.CacheStrategy cs = null,
        org.apache.commons.vfs2.FilesCache fc = null
    ) {
        this.setLogger( vfslogger )

        if(cs) {
            this.cacheStrategy = cs
        }
        if(fc) {
            this.filesCache = fc
        }

        if(replicator == null &&  tfs == null) {
            _configureReplicatorAndFileStore()
        } else {
            _configureReplicatorAndFileStore(  tfs, replicator )
        }

        if(legacyMode) {
            _configurePlugins(PLUGIN_DEFAULT_RESOURCE)
        } else {
            loadFromProviderSpec( providerSpec )
        }

        if(scanForVfsXml) {
            _configurePlugins()
        }

        super.init()
    }

    /**
     * Initializes this manager.  Adds the providers and replicator.
     * @throws FileSystemException if an error occurs.
     */
    @Override
    void init()
    {
        init(ProviderSpecification.DEFAULT_PROVIDERS,false,false)
    }

    /**
     * Adds a provider from a provider definition.
     * @param providerDef the provider definition
     * @param isDefault true if the default should be used.
     * @throws FileSystemException if an error occurs.
     */
    private boolean addProvider(final Provider provider, final boolean isDefault=false)
    {
        assert provider.className
        assert provider.className.size() > 0

        loggerInstance().debug "Processing provider: ${provider.className} S${provider.schemes} DS${provider.dependsOnSchemes} DC${provider.dependsOnClasses}"
        if( provider.dependsOnSchemes?.any { String it ->
            if(!hasProvider(it) ) {
                loggerInstance().debug "Skipping ${provider.className} due to missing scheme '${it}'"
                true
            } else {
                false
            }
        } ) {
            return false
        }

        ClassLoader cl = _classLoader()
        if( provider.dependsOnClasses?.any { String it ->
            try {
                cl.loadClass(it)
                false
            }
            catch(final ClassNotFoundException e) {
                loggerInstance().debug "Skipping ${provider.className} due to missing class '${it}'"
                true
            }
        } ) { return false }


        // Create and register the provider
        FileProvider fp
        try {
            fp = createFileProvider(provider.className)
            if(provider.schemes?.size() > 0) {
                addProvider(provider.schemes as String[],fp)
            }
        } catch(final FileSystemException e) {
            loggerInstance().debug "Skipping ${provider.className} due to missing class: `${e}`"
            return false
        }

        if (isDefault)
        {
            super.setDefaultProvider(fp)
        }

        return true
    }

    /** Creates a file provider instance from a Provider description
     *
     * @param provider
     * @return FileProvider
     */
    private FileProvider createFileProvider(String className) {
        _createInstance(className) as FileProvider
    }


    /**
     * Adds a operationProvider from a operationProvider definition.
     */
    private boolean addOperationProvider(final OperationProvider provider)
    {
        provider.schemes?.each { String it ->
            if(hasProvider(it)) {
                final FileOperationProvider op = _createInstance(provider.className) as FileOperationProvider
                addOperationProvider(it, op)
            } else {
                loggerInstance().debug "Skipping operation provider ${provider.className} due to missing scheme ${it}"
            }
        }
    }

    private ClassLoader _classLoader()
    {
//        if (classLoader != null)
//        {
//            return classLoader;
//        }

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null)
        {
            cl = getClass().getClassLoader();
        }

        return cl;
    }

    private void loadFromProviderSpec (final ProviderSpecification providerSpec) {
        providerSpec.providers.each { Provider it ->  addProvider(it) }
        providerSpec.operationProviders.each { OperationProvider it -> addOperationProvider(it) }
        addProvider(providerSpec.defaultProvider,true)
        providerSpec.mimeTypes.each { MimeType it -> addMimeTypeMap(it.key, it.scheme) }
        providerSpec.extensions.each { Extension it -> addExtensionMap(it.extension,it.scheme) }
    }

    /**
     * Creates a provider.
     */
    private Object _createInstance(final String className)
    {
        try
        {
            final Class<?> clazz = _classLoader().loadClass(className);
            return clazz.newInstance();
        }
        catch (final Exception e)
        {
            throw new FileSystemException("Cannot create a provider instance", className, e);
        }
    }

    /**
     * Backwards compatibility with Apache VFS. Allows for scanning of classpath to look for
     * /META-INF/vfs-providers.xml files
     * @throws FileSystemException if an error occurs.
     */
    private void _configurePlugins( final String path = PLUGIN_CONFIG_RESOURCE)
    {
        final ClassLoader cl = _classLoader()

        Enumeration<URL> enumResources
        try
        {
            enumResources = cl.getResources(path)
        }
        catch (final IOException e)
        {
            throw new FileSystemException(e)
        }

        while (enumResources.hasMoreElements())
        {
            final URL url = enumResources.nextElement();
            _loadPlugin(url);
        }
    }

    private void _loadPlugin(URL u) {
        try {
            loadFromProviderSpec(VfsXml.createProviderSpec(u))
        }
        catch (final MapParseException e) {
            loggerInstance().debug "Skipping loading of plugin ${u}: ${e}"
        }
    }


    private void _configureReplicatorAndFileStore( TemporaryFileStore tfs, FileReplicator fr  ) {
        assert fr != null || tfs != null
        if (fr == null ) {
            this.replicator= new PrivilegedFileReplicator(new DefaultFileReplicator())
            this.temporaryFileStore = tfs
        } else if(tfs == null) {
            this.replicator = new PrivilegedFileReplicator(fr)
            this.temporaryFileStore = new DefaultFileReplicator()
        } else{
            this.replicator = new PrivilegedFileReplicator(fr)
            this.temporaryFileStore = tfs
        }
   }

    private void _configureReplicatorAndFileStore( ) {
        final DefaultFileReplicator dfr = new DefaultFileReplicator()
        this.replicator = new PrivilegedFileReplicator(dfr)
        this.temporaryFileStore = dfr
    }
}


//    <provider class-name="org.apache.commons.vfs2.provider.tar.TarFileProvider">
//        <scheme name="tgz"/>
//    <if-available scheme="gz"/>
//    <if-available scheme="tar"/>
//    </provider>

//    <providers>
//    <default-provider class-name="org.apache.commons.vfs2.provider.url.UrlFileProvider">
//    </default-provider>
//    <provider class-name="org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider">
//        <scheme name="file"/>
//    </provider>
//    <provider class-name="org.apache.commons.vfs2.provider.zip.ZipFileProvider">
//        <scheme name="zip"/>
//    </provider>
//    <provider class-name="org.apache.commons.vfs2.provider.tar.TarFileProvider">
//        <scheme name="tar"/>
//    <if-available class-name="org.apache.commons.compress.archivers.tar.TarArchiveOutputStream"/>
//    </provider>
//
//    <provider class-name="org.apache.commons.vfs2.provider.bzip2.Bzip2FileProvider">
//        <scheme name="bz2"/>
//    <if-available class-name="org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream"/>
//    </provider>
//    <provider class-name="org.apache.commons.vfs2.provider.gzip.GzipFileProvider">
//        <scheme name="gz"/>
//    </provider>
//
//    <provider class-name="org.apache.commons.vfs2.provider.jar.JarFileProvider">
//        <scheme name="jar"/>
//    <scheme name="sar"/>
//    <scheme name="ear"/>
//    <scheme name="par"/>
//    <scheme name="ejb3"/>
//    <scheme name="war"/>
//    </provider>
//    <provider class-name="org.apache.commons.vfs2.provider.temp.TemporaryFileProvider">
//        <scheme name="tmp"/>
//    </provider>
//    <provider class-name="org.apache.commons.vfs2.provider.ftp.FtpFileProvider">
//        <scheme name="ftp"/>
//    <if-available class-name="org.apache.commons.net.ftp.FTPFile"/>
//    </provider>
//    <provider class-name="org.apache.commons.vfs2.provider.ftps.FtpsFileProvider">
//        <scheme name="ftps"/>
//    <if-available class-name="org.apache.commons.net.ftp.FTPFile"/>
//    </provider>
//    <provider class-name="org.apache.commons.vfs2.provider.http.HttpFileProvider">
//        <scheme name="http"/>
//    <if-available class-name="org.apache.commons.httpclient.HttpClient"/>
//    </provider>
//    <provider class-name="org.apache.commons.vfs2.provider.https.HttpsFileProvider">
//        <scheme name="https"/>
//    <if-available class-name="org.apache.commons.httpclient.HttpClient"/>
//    </provider>
//    <provider class-name="org.apache.commons.vfs2.provider.sftp.SftpFileProvider">
//        <scheme name="sftp"/>
//    <if-available class-name="javax.crypto.Cipher"/>
//    <if-available class-name="com.jcraft.jsch.JSch"/>
//    </provider>
//    <provider class-name="org.apache.commons.vfs2.provider.res.ResourceFileProvider">
//        <scheme name="res"/>
//    </provider>
//        <provider class-name="org.apache.commons.vfs2.provider.webdav.WebdavFileProvider">
//        <scheme name="webdav"/>
//    <if-available class-name="org.apache.commons.httpclient.HttpClient"/>
//    <if-available class-name="org.apache.jackrabbit.webdav.client.methods.DavMethod"/>
//    </provider>
//    <!--
//    <provider class-name="org.apache.commons.vfs2.provider.svn.SvnFileProvider">
//        <scheme name="svnhttps"/>
//    </provider>
//    -->
//    <!--
//        <provider class-name="org.apache.commons.vfs2.provider.tar.TgzFileProvider">
//            <scheme name="tgz"/>
//    <if-available scheme="gz"/>
//    <if-available scheme="tar"/>
//    </provider>
//        <provider class-name="org.apache.commons.vfs2.provider.tar.Tbz2FileProvider">
//            <scheme name="tbz2"/>
//    <if-available scheme="bz2"/>
//    <if-available scheme="tar"/>
//    </provider>
//    -->
//    <provider class-name="org.apache.commons.vfs2.provider.tar.TarFileProvider">
//        <scheme name="tgz"/>
//    <if-available scheme="gz"/>
//    <if-available scheme="tar"/>
//    </provider>
//    <provider class-name="org.apache.commons.vfs2.provider.tar.TarFileProvider">
//        <scheme name="tbz2"/>
//    <if-available scheme="bz2"/>
//    <if-available scheme="tar"/>
//    </provider>
//    <provider class-name="org.apache.commons.vfs2.provider.ram.RamFileProvider">
//        <scheme name="ram"/>
//    </provider>
//    <provider class-name="org.apache.commons.vfs2.provider.hdfs.HdfsFileProvider">
//        <scheme name="hdfs"/>
//    <if-available class-name="org.apache.hadoop.fs.FileSystem"/>
//    </provider>
//
//    <extension-map extension="zip" scheme="zip"/>
//    <extension-map extension="tar" scheme="tar"/>
//    <mime-type-map mime-type="application/zip" scheme="zip"/>
//    <mime-type-map mime-type="application/x-tar" scheme="tar"/>
//    <mime-type-map mime-type="application/x-gzip" scheme="gz"/>
//    <!--
//    <mime-type-map mime-type="application/x-tgz" scheme="tgz"/>
//    -->
//    <extension-map extension="jar" scheme="jar"/>
//    <extension-map extension="bz2" scheme="bz2"/>
//    <extension-map extension="gz" scheme="gz"/>
//    <!--
//    <extension-map extension="tgz" scheme="tgz"/>
//    <extension-map extension="tbz2" scheme="tbz2"/>
//    -->
//    <extension-map extension="tgz" scheme="tar"/>
//    <extension-map extension="tbz2" scheme="tar"/>
//
//    <!--
//    <filter-map class-name="org.apache.commons.vfs2.content.bzip2.Bzip2Compress">
//    <extension name="bz2"/>
//    <extension name="tbz2"/>
//    <if-available class-name="org.apache.commons.compress.bzip2.CBZip2InputStream"/>
//    </filter-map>
//    <filter-map class-name="org.apache.commons.vfs2.content.gzip.GzipCompress">
//        <extension name="gz"/>
//    <extension name="tgz"/>
//    <mime-type name="application/x-tgz" />
//    </filter-map>
//    -->
//</providers>
//<provider class-name="org.apache.commons.vfs2.provider.smb.SmbFileProvider">
//<scheme name="smb"/>
//<if-available class-name="jcifs.smb.SmbFile"/>
//</provider>
//
//        <provider class-name="org.apache.commons.vfs2.provider.mime.MimeFileProvider">
//                <scheme name="mime"/>
//<if-available class-name="javax.mail.internet.MimeMultipart"/>
//</provider>

//        <extension-map extension="mime" scheme="mime"/>
//<mime-type-map mime-type="message/rfc822" scheme="mime"/>
