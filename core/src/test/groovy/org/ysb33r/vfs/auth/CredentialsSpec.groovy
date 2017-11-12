package org.ysb33r.vfs.auth

import spock.lang.Specification

import java.security.InvalidParameterException

class CredentialsSpec extends Specification {

    def "Create basic authentication supplier withj username & password"() {
        given:
        BasicCredentials creds = Credentials.fromUsernamePassword('foo','bar')

        expect:
        creds.username == 'foo'
        creds.password == 'bar'
    }

    def "Create basic authentication supplier with username only"() {
        given:
        BasicCredentials creds = Credentials.fromUsernamePassword('foo',null)

        expect:
        creds.username == 'foo'
        creds.password == null
    }

    def "Username cannot be null for a basic authentication supplier"() {
        when:
        BasicCredentials creds = Credentials.fromUsernamePassword(null,null)

        then:
        thrown(InvalidParameterException)
    }
}