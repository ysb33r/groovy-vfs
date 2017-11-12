package org.ysb33r.vfs.core

import org.ysb33r.vfs.auth.BasicCredentials
import org.ysb33r.vfs.auth.CredentialsSupplier
import org.ysb33r.vfs.core.helpers.CoreBaseSpecification
import spock.lang.Unroll

class VfsURISpec extends CoreBaseSpecification {

    static final File testFsReadOnlyRoot = CoreBaseSpecification.testFsReadOnlyRoot

    @Unroll
    void 'Create URI from #type'() {
        when:
        FileSystemOptions opts = new FileSystemOptions()
        opts.add 'vfs.ftp.passiveMode',true
        VfsURI uri = new VfsURI(input)

        then:
        uri.path == testFsReadOnlyRoot.toPath()
        uri.getUri() == testFsReadOnlyRoot.absoluteFile.toURI()
        uri.properties.size() == 0
        uri.name == testFsReadOnlyRoot.name

        when:
        if(takesOptions) {
            uri = new VfsURI(input,opts)
        }

        then:
        uri.path == testFsReadOnlyRoot.toPath()
        uri.getUri() == testFsReadOnlyRoot.absoluteFile.toURI()
        uri.properties.size() == 0
        uri.name == testFsReadOnlyRoot.name

        where:
        type     | input                                              | takesOptions
        'String' | testFsReadOnlyRoot.absoluteFile.toURI().toString() | true
        'GString'| "${testFsReadOnlyRoot.absoluteFile.toURI()}"       | true
        'URI'    | testFsReadOnlyRoot.toURI()                         | true
        'URL'    | testFsReadOnlyRoot.toURI().toURL()                 | true
        'Path'   | testFsReadOnlyRoot.toPath()                        | false
        'File'   | testFsReadOnlyRoot                                 | false
    }

    void 'URI with password should not display passwords'() {
        when:
        VfsURI uri = new VfsURI( 'http://foo:bar@example.com/path/to/file')

        then:
        uri.friendlyURI == 'http://foo:*****@example.com/path/to/file'
    }

    void 'A relative path can be added to an existing URI'() {
        given:
        VfsURI root = new VfsURI(testFsReadOnlyRoot)
        File target = new File(testFsReadOnlyRoot,'foo/bar')

        when:
        VfsURI uri = root.resolve('foo/bar')

        then:
        uri.getUri() == target.toURI()
        uri.path == target.toPath()

        when:
        uri = new VfsURI(root,'foo/bar')

        then:
        uri.getUri() == target.toURI()
        uri.path == target.toPath()
    }

    void 'URIs created without user info will have no associated credentials'() {

        when: 'Any VfsURI is created without user info in the URI'
        VfsURI root = new VfsURI(testFsReadOnlyRoot)

        then: 'There will be no associated credentials'
        root.credentials == null

    }

    void 'Adding a credentials supplier to a URI'() {

        given: "Any created URI"
        VfsURI root = new VfsURI(testFsReadOnlyRoot)

        and: 'A credentials supplier'
        CredentialsSupplier creds = new CredentialsSupplier() {
            @Override
            String toString() {
                return super.toString()
            }
        }

        when: 'A credentials object is added'
        root.credentials = creds

        then: 'There will be associated credentials'
        root.credentials == creds

        when: 'The credentials are removed from the URI'
        root.removeCredentials()

        then: 'There is no more associated credentials'
        root.credentials == null
    }

    void 'Simple credentials can be added via a username/password combination'() {
        given: "Any created URI"
        VfsURI root = new VfsURI(testFsReadOnlyRoot)

        when: 'Username & password is added'
        root.setCredentials('foo','bar')

        then:
        root.credentials != null
        root.credentials instanceof BasicCredentials
        root.credentials.username == 'foo'
        root.credentials.password == 'bar'
    }

    void 'A URI with userinfo will automatically add credentials'() {
        when: 'A URI is supplied with username & password'
        VfsURI root = new VfsURI( 'http://foo:bar@example.com/path/to/file')

        then: 'The credentials are added to the object'
        root.credentials != null
        root.credentials instanceof BasicCredentials
        root.credentials.username == 'foo'
        root.credentials.password == 'bar'

        and: 'The credentials are removed from the URI by default'
        root.uri == 'http://example.com/path/to/file'
    }

    void 'When credentials are of basic credentials it can be added to URI'() {
        when: 'A URI is supplied without username & password'
        VfsURI root = new VfsURI( 'http://example.com/path/to/file')

        and: 'The credentials are added afterwards'
        root.setCredentials('foo','b@r')

        then: 'The URI does not contain them'
        root.uri.userInfo == null

        and: 'A URI containing credentials can be shown'
        root.uriWithCredentials.userInfo == 'foo:b%40r'
    }

}