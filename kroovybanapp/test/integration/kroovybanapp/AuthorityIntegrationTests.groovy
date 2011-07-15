package kroovybanapp

import org.kroovyban.Authority

import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class AuthorityIntegrationTests {

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

        def a = new Authority()
        assert a.authority == "sysadmin" : "Expecting only initial role to be 'sysadmin'"
    }
}
