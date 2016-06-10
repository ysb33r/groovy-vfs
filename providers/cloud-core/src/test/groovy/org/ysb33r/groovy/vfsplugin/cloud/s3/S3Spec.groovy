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
package org.ysb33r.groovy.vfsplugin.cloud.s3

import org.apache.commons.vfs2.Capability
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSelectInfo
import org.apache.commons.vfs2.FileSelector
import org.apache.commons.vfs2.Selectors
import org.apache.commons.vfs2.VFS
import org.apache.commons.vfs2.provider.LocalFileProvider
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider
import org.apache.commons.vfs2.util.FileObjectUtils
import org.jclouds.ContextBuilder
import org.jclouds.blobstore.BlobStore
import org.jclouds.blobstore.BlobStoreContext
import org.jclouds.blobstore.domain.StorageMetadata
import org.jclouds.blobstore.domain.StorageType
import org.jclouds.blobstore.options.ListContainerOptions


import static org.apache.commons.vfs2.Capability.*

import org.apache.commons.vfs2.FileSystemException
import org.apache.commons.vfs2.FileSystemManager
import org.apache.commons.vfs2.FileSystemOptions
import org.apache.commons.vfs2.FileType
import org.apache.commons.vfs2.impl.DefaultFileSystemManager
import org.apache.commons.vfs2.provider.UriParser

import spock.lang.*

/**
 * Created by schalkc on 07/05/2014.
 */
class S3Spec extends Specification {

    static final String RUNPREFIX = System.getProperty("RUNPREFIX") ?: 'zzz_'
    static final File TESTFSREADROOT = new File(System.getProperty('TESTFSREADROOT') ?: 'src/test/resources/test-files')
    static final File TESTFSWRITEROOT = new File( "${System.getProperty('TESTFSWRITEROOT') ?: 'src/test/resources/test-files'}/s3" )
    static final String S3ID = System.getProperty("S3ID")
    static final String S3KEY = System.getProperty("S3KEY")
    static final String S3BUCKET = System.getProperty("S3BUCKET")
    static final String S3ID_ENC = UriParser.encode(S3ID)
    static final String S3KEY_ENC = UriParser.encode(S3KEY, '/@:'.toCharArray())
    static final String SCHEME = 's3'
    static final String S3BUCKET_ENC = UriParser.encode(S3BUCKET)
    static final String S3ROOT_ENC = "s3://${S3ID_ENC}:${S3KEY_ENC}@${S3BUCKET_ENC}"
    static final boolean RUNTESTS = !System.getProperty('TEST.OFFLINE') && System.getProperty("S3ID") && System.getProperty("S3KEY") && System.getProperty("S3BUCKET")

    @Shared BlobStoreContext specContext
    @Shared boolean hasLogsFolder

    static FileSystemManager createFileSystemManager() {
        def fs = new DefaultFileSystemManager()
        fs.addProvider(SCHEME, new S3FileProvider())
        fs.addProvider('file',new DefaultLocalFileProvider())
        fs.init()
        return fs
    }

    static S3FileSystem createFileSystem() {
        def fp = new S3FileProvider()
        fp.doCreateFileSystem(fp.parseUri(null, S3ROOT_ENC), new FileSystemOptions())
    }

    static FileObject[] getRootFiles(def fsm) {
        fsm.resolveFile("${S3ROOT_ENC}").findFiles( [
                includeFile : { FileSelectInfo fsi ->
                    fsi.depth == 1 && !(fsi.file.name.pathDecoded ==~ /\/*logs/) && fsi.file.name.pathDecoded =~ /^\/*${RUNPREFIX}/
                },
                traverseDescendents : { fsi ->
                    fsi.depth < 2
                }
        ] as FileSelector
        )

    }

    void setupSpec() {
        if(!System.getProperty('TEST.OFFLINE')) {
            specContext = ContextBuilder.newBuilder('aws-s3').credentials(S3ID, S3KEY).buildView(BlobStoreContext.class)
            hasLogsFolder = specContext.blobStore.directoryExists(S3BUCKET, 'logs')
        }
    }

    void setup() {

        if(TESTFSWRITEROOT.exists()) {
            TESTFSWRITEROOT.deleteDir()
        }

        TESTFSWRITEROOT.mkdirs()

        if(!System.getProperty('TEST.OFFLINE')) {
            def lco = (new ListContainerOptions()).recursive()
            def listing
            while (!listing || listing.nextMarker) {
                if(listing) {
                    lco = lco.afterMarker(listing.nextMarker)
                }
                listing=specContext.blobStore.list(S3BUCKET,lco)
                listing.each { StorageMetadata sm ->
                    if (sm.name.startsWith(RUNPREFIX)) {
                        switch (sm.type) {
                            case StorageType.FOLDER:
                            case StorageType.RELATIVE_PATH:
                                specContext.blobStore.deleteDirectory(S3BUCKET, sm.name)
                                break
                            case StorageType.BLOB:
                                specContext.blobStore.removeBlob(S3BUCKET, sm.name)
                                break
                        }
                    }
                }

            }
        }
    }

    def "Create a S3FileProvider"() {
        given:
            def fp = new S3FileProvider()

        expect:
            fp != null

    }

    def "Create a FileSystem for the root of the bucket"() {
        given:
            def fp = new S3FileProvider()
            def fn = fp.parseUri(null, "${SCHEME}://${S3ID_ENC}:${S3KEY_ENC}@${S3BUCKET_ENC}")
            def fs = fp.doCreateFileSystem(fn, new FileSystemOptions())

        expect:
            fs != null
            fs instanceof S3FileSystem
            fs.rootURI == "${SCHEME}://${S3ID_ENC}:${S3KEY_ENC}@${S3BUCKET_ENC}/"
            fs.lastModTimeAccuracy < 1
            fs.parentLayer == null
// TODO:           fs.rootName ==
// TODO:           fs.root == fn

    }

    def "Must not be able to create a junction in S3"() {
        given:
            def fs = createFileSystem()

        when:
            fs.addJunction('foo', null)

        then:
            thrown(FileSystemException)
    }

    def "S3 Capability checks"() {
        given:
            def fsm = createFileSystemManager()
            def caps = fsm.getProviderCapabilities('s3')

        expect:
            caps.contains(cap) == hasCap

        where:
            cap           | hasCap
            LIST_CHILDREN | true
            CREATE        | true
            DELETE        | true
            GET_TYPE      | true
            GET_LAST_MODIFIED | true
            WRITE_CONTENT | true
            JUNCTIONS     | false
            SET_LAST_MODIFIED_FILE | false
            SET_LAST_MODIFIED_FOLDER | false
    }

    def "Base File Object should be root"() {
        given:
            def fsm = createFileSystemManager()
            FileObject fo = fsm.resolveFile(S3ROOT_ENC)

        expect:
            fo != null
            fo.fileSystem.root == fo
            fo.name.baseName.size() == 0
            fo.type == FileType.FOLDER
            fo.exists()
            fo.name.friendlyURI == "${SCHEME}://${S3ID_ENC}:***@${S3BUCKET_ENC}/"
            // TODO: Enabled this for VFS 2.1
            //fo.publicURIString == "${SCHEME}://${S3ID_ENC}:***@${S3BUCKET_ENC}/"

    }

    def 'Creating a filename should not prefix to slashes to path'() {
        given:
            def fn1 = new S3FileName( 'foo','bucket','user','pass','path',FileType.FOLDER )
            def fn2 = new S3FileName( 'foo','bucket','user','pass','path/file',FileType.FILE )
            def fn3 = new S3FileName( 'foo','bucket','user','pass','/path',FileType.FOLDER )
            def fn4 = new S3FileName( 'foo','bucket','user','pass','/path/file',FileType.FILE )

        expect:
            fn1.path == 'path'
            fn2.path == 'path/file'
            fn3.path == '/path'
            fn4.path == '/path/file'
    }

    @IgnoreIf({ !RUNTESTS })
    def "Check for non-existing file"() {
        given:
            def fsm = createFileSystemManager()
            def fo = fsm.resolveFile("${S3ROOT_ENC}/${RUNPREFIX}foobar")

        expect:
            fo != null
            fo.name.baseName.size() == "${RUNPREFIX}foobar".size()
            fo.type == FileType.IMAGINARY
            !fo.exists()
            fo.publicURIString == "${SCHEME}://${S3ID_ENC}:***@${S3BUCKET_ENC}/${RUNPREFIX}foobar"
            fo.name.getURI() == "${SCHEME}://${S3ID_ENC}:${S3KEY_ENC}@${S3BUCKET_ENC}/${RUNPREFIX}foobar"
    }

    @IgnoreIf({ !RUNTESTS })
    def "List root folder should return empty (besides a possible logs folder)"() {
        given:
            def fsm = createFileSystemManager()
            def fos = getRootFiles(fsm)

        expect:
            !fos.size()

    }

    @IgnoreIf({ !RUNTESTS })
    def "Must be able to create a file"() {
        given:
            def fsm = createFileSystemManager()
            def fo = fsm.resolveFile("${S3ROOT_ENC}/${RUNPREFIX}foobar")
            assert !fo.exists()
            fo.createFile()

        expect: "Blob to exist and is a file (tested with jCloud directly)"
            specContext.blobStore.blobExists(S3BUCKET,"${RUNPREFIX}foobar")
            specContext.blobStore.blobMetadata(S3BUCKET,"${RUNPREFIX}foobar").type == StorageType.BLOB

        and: "Check that FileObject is correctly attached"
            fo.isAttached()

        and: "Check that the FileObject reports that the empty file exists"
            fo.exists()

        and: "Check that is is reported as a file"
            fo.type == FileType.FILE
    }

    @IgnoreIf({ !RUNTESTS })
    def "Must be able to create a directory"() {
        given:
            def fsm = createFileSystemManager()
            def fo = fsm.resolveFile("${S3ROOT_ENC}/${RUNPREFIX}foodir")
            assert !fo.exists()
            fo.createFolder()

        expect: "Blob to exist and is a directory (tested with jCloud directly)"
            specContext.blobStore.directoryExists(S3BUCKET,"${RUNPREFIX}foodir")

        and: "Check that FileObject is correctly attached"
            fo.isAttached()

        and: "Check that the FileObject reports that the directory exists"
            fo.exists()

        and: "Check that is is reported as a directory"
            fo.type == FileType.FOLDER
    }

    @IgnoreIf({ !RUNTESTS })
    def "Must be able to create a directory, then a file in that directory"() {
        given:
            def fsm = createFileSystemManager()
            def fo1 = fsm.resolveFile("${S3ROOT_ENC}/${RUNPREFIX}foodir")
            def fo2 = fsm.resolveFile("${S3ROOT_ENC}/${RUNPREFIX}foodir/${RUNPREFIX}foofile")
            assert !fo1.exists()
            assert !fo2.exists()
            fo1.createFolder()
            fo2.createFile()

        expect: "Blob to exist and is a file (tested with jCloud directly)"
            specContext.blobStore.directoryExists(S3BUCKET,"${RUNPREFIX}foodir")
            specContext.blobStore.blobExists(S3BUCKET,"${RUNPREFIX}foodir/${RUNPREFIX}foofile")
            specContext.blobStore.blobMetadata(S3BUCKET,"${RUNPREFIX}foodir/${RUNPREFIX}foofile").type == StorageType.BLOB

        and: "Check that FileObjects are correctly attached"
            fo1.isAttached()
            fo2.isAttached()

        and: "Check that the FileObject reports that the empty file exists"
            fo1.exists()
            fo2.exists()

        and: "Check that types are correctly reported"
            fo1.type == FileType.FOLDER
            fo2.type == FileType.FILE

        and: "Check that root listing only contains 1 folder"
            getRootFiles(fsm).size() == 1

        and: "Check that newly created folder only contains one child"
            fo1.children.size() == 1
    }

    @IgnoreIf({ !RUNTESTS })
    def "Must be able to create a file in a directory, then a delete it"() {
        given:
            def fsm = createFileSystemManager()
            def fo1 = fsm.resolveFile("${S3ROOT_ENC}/${RUNPREFIX}foodir")
            def fo2 = fsm.resolveFile("${S3ROOT_ENC}/${RUNPREFIX}foodir/${RUNPREFIX}foofile")
            assert !fo1.exists()
            assert !fo2.exists()
            fo1.createFolder()
            fo2.createFile()
            assert specContext.blobStore.blobExists(S3BUCKET, "${RUNPREFIX}foodir/${RUNPREFIX}foofile")
            fo2.delete()

        expect: "Blob to no longer exist (via jClouds)"
            !specContext.blobStore.blobExists(S3BUCKET, "${RUNPREFIX}foodir/${RUNPREFIX}foofile")

        and: "That the file type has changed"
            fo2.type == FileType.IMAGINARY

    }

    @IgnoreIf({ !RUNTESTS })
    def "Renaming files are not supported"() {
        given:
            def fsm = createFileSystemManager()
            def fo1 = fsm.resolveFile("${S3ROOT_ENC}/${RUNPREFIX}foofile1")
            def fo2 = fsm.resolveFile("${S3ROOT_ENC}/${RUNPREFIX}foofile2")
            assert !fo1.exists()
            assert !fo2.exists()
            fo1.createFile()

        expect:
            !fo1.canRenameTo(fo2)
    }

    @IgnoreIf({ !RUNTESTS })
    def "Deleting a file must be possible"() {
        given:
            def fsm = createFileSystemManager()
            def fo1 = fsm.resolveFile("${S3ROOT_ENC}/${RUNPREFIX}foofile1")
            assert !fo1.exists()
            fo1.createFile()
            assert fo1.exists()

        expect:
            fo1.delete() == true
            fo1.exists() == false

    }


    @IgnoreIf({ !RUNTESTS })
    def "Resolve one file relative to another"() {
        given:
            assert !S3ROOT_ENC.endsWith('/')
            def fsm = createFileSystemManager()
            def root = fsm.resolveFile("${S3ROOT_ENC}")
            def fo1 = root.resolveFile("${RUNPREFIX}foodir1")
            def fo12 = fo1.resolveFile("${RUNPREFIX}foofile2")

        expect:
            root.name.path       == '/'
            fo1.name.path        == "/${RUNPREFIX}foodir1"
            fo12.name.path       == "/${RUNPREFIX}foodir1/${RUNPREFIX}foofile2"
            root.publicURIString == "${SCHEME}://${S3ID_ENC}:***@${S3BUCKET_ENC}/"
            fo1.publicURIString  == "${SCHEME}://${S3ID_ENC}:***@${S3BUCKET_ENC}/${RUNPREFIX}foodir1"
            fo12.publicURIString == "${SCHEME}://${S3ID_ENC}:***@${S3BUCKET_ENC}/${RUNPREFIX}foodir1/${RUNPREFIX}foofile2"
            fo1.parent           == root
            fo12.parent          == fo1
    }

    @IgnoreIf({ !RUNTESTS })
    def "Create two folders, then list their children"() {
        given:
            def fsm = createFileSystemManager()
            def fo1 = fsm.resolveFile("${S3ROOT_ENC}/${RUNPREFIX}foodir1")
            def fo11 = fsm.resolveFile("${S3ROOT_ENC}/${RUNPREFIX}foodir1/${RUNPREFIX}foofile1")
            def fo12 = fsm.resolveFile("${S3ROOT_ENC}/${RUNPREFIX}foodir1/${RUNPREFIX}foofile2")
            def fo2 = fsm.resolveFile("${S3ROOT_ENC}/${RUNPREFIX}foodir2")

        when:
            fo1.createFolder()
            fo2.createFolder()
            fo11.createFile()
            fo12.createFile()
            def children1 = fo1.children

        then:
            fo1.exists()
            fo2.exists()
            children1.size() == 2
            children1.length == 2 // size() should be equiv of length, but I'm am paranoid,
            children1.find { it.name.toString() == "${S3ROOT_ENC}/${RUNPREFIX}foodir1/${RUNPREFIX}foofile1".toString() }
            children1.find { it.name.toString() == "${S3ROOT_ENC}/${RUNPREFIX}foodir1/${RUNPREFIX}foofile2".toString() }

        when:
            def children2 = fo2.children

        then:
            children2.size() == 0
            children2.length == 0

    }

    @IgnoreIf({ !RUNTESTS })
    def "Select a file using SELECT_SELF"() {
        given:
            def fsm = createFileSystemManager()
            def fo1 = fsm.resolveFile("${S3ROOT_ENC}/${RUNPREFIX}foodir1")
            fo1.createFolder()
            assert fo1.exists()

            FileObject[] find1
            List<FileObject> find2 = []

        when:
            find1 = fo1.findFiles(Selectors.SELECT_SELF)
            fo1.findFiles(Selectors.SELECT_SELF,true,find2)

        then:
            find1.size() == 1
            find2.size() == 1
            find1[0] == fo1
            find2[0] == fo1
            find2[0].type.hasChildren()
            FileObjectUtils.getAbstractFileObject(find2[0]).children.length == 0
    }

    @IgnoreIf({ !RUNTESTS })
    def "Deleting an directory must be possible via delete(), but other directories only via deleteAll()"() {
        given:
            def fsm = createFileSystemManager()
            def fo1 = fsm.resolveFile("${S3ROOT_ENC}/${RUNPREFIX}foodir1")
            def fo2 = fsm.resolveFile("${S3ROOT_ENC}/${RUNPREFIX}foodir2")
            def fo3 = fsm.resolveFile("${S3ROOT_ENC}/${RUNPREFIX}foodir2/${RUNPREFIX}foofile2")
            //def fo3 = fo2.resolveFile("${RUNPREFIX}foofile2")
            assert !fo1.exists()
            assert !fo2.exists()
            assert !fo3.exists()

        when: "Create one empty folder and one folder containing a filer"
            fo1.createFolder()
            fo2.createFolder()
            fo3.createFile()

        then: "All 3 should exist"
            fo1.exists() == true
            fo2.exists() == true
            fo3.exists() == true

        when: "Deleting the empty folder"
            def fo1Delete = fo1.delete()

        then: "Folder should be deleted"
            fo1Delete == true
            fo1.exists() == false

        when: "Deleting the non-empty folder with delete()"
            def fo2Delete = fo2.delete()

        then: "Folder should not be deleted, not should it's child file"
            fo2Delete == false
            fo2.exists() == true
            fo3.exists() == true

        when: "Deleting the non-empty folder with deleteAll()"
            def fo2DeleteAll = fo2.deleteAll()

        then: "Two items should be deleted"
            fo2DeleteAll == 2
            fo2.exists() == false
            fo3.exists() == false


    }

    @IgnoreIf({ !RUNTESTS })
    def "Upload a file, check its size. Download the file, check size again"() {
        given:
            def fsm = createFileSystemManager()
            def fo_s3    = fsm.resolveFile("${S3ROOT_ENC}/${RUNPREFIX}file1.txt")
            def fo_read  = fsm.resolveFile(TESTFSREADROOT,'file1.txt')
            def fo_write = fsm.resolveFile(TESTFSWRITEROOT,'file1w.txt')
            assert !fo_s3.exists()
            assert !fo_write.exists()
            long size_read = fo_read.content.size
            fo_s3.copyFrom(fo_read,Selectors.SELECT_SELF)
            assert fo_s3.exists()
            fo_write.copyFrom(fo_s3,Selectors.SELECT_SELF)

        expect:
            fo_s3.content.size == fo_read.content.size
            fo_write.content.size == fo_read.content.size

    }

    @IgnoreIf({ !RUNTESTS })
    def "Setting file modified time is not supported"() {
        given:
            def fsm = createFileSystemManager()
            def fo_s3    = fsm.resolveFile("${S3ROOT_ENC}/${RUNPREFIX}file5.txt")
            def fo_read  = fsm.resolveFile(TESTFSREADROOT,'file1.txt')

        when:
            fo_s3.copyFrom(fo_read,Selectors.SELECT_SELF)
            fo_s3.content.lastModifiedTime = 10000

        then:
            thrown(FileSystemException)
    }

    @IgnoreIf({ !RUNTESTS })
    def "Making the file executable not supported"() {
        given:
            def fsm = createFileSystemManager()
            def fo_s3    = fsm.resolveFile("${S3ROOT_ENC}/${RUNPREFIX}file5.txt")
            def fo_read  = fsm.resolveFile(TESTFSREADROOT,'file1.txt')
            fo_s3.copyFrom(fo_read,Selectors.SELECT_SELF)

        expect:
            !fo_s3.setExecutable(true,true)
            !fo_s3.setExecutable(false,true)
            !fo_s3.setExecutable(true,false)
            !fo_s3.setExecutable(false,false)

    }
}