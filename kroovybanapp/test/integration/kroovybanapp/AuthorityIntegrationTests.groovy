package kroovybanapp

import org.kroovyban.Authority

import org.junit.*
import org.codehaus.groovy.grails.commons.GrailsApplication

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
class AuthorityIntegrationTests {

    def grailsApplication

    @Before
    void setUp() {
        // Setup logic here
    }

    @After
    void tearDown() {
        // Tear down logic here
    }
    @Test
    void testBootStrap() {
        assert Authority.count() == 1 : "Expecting only role to be added during bootStrap"

        def a = Authority.get(1)
        assert a != null
        assert a.authority == "ROLE_SYSADMIN" : "Expecting only initial role to be 'ROLE_SYSADMIN'"
    }

	}
