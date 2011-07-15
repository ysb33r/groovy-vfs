package kroovybanapp

import org.kroovyban.User

import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class UserIntegrationTests {

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
        def sprintSecurityService

        def username = grailsApplication.config.bootstrap.init.sysadmin
        def password = sprintSecurityService.encodePassword( 'password',username )
        assert User.count() == 1 : "Expecting only 1 user to be added during bootStrap"

        def a = User.get(1)
        assert a != null
        assert a.username == username : "Expecting only initial role to be 'sysadmin'"
        assert a.password == password : "Expecting initial password for initial user to be encoded version of 'password'"
    }
}
