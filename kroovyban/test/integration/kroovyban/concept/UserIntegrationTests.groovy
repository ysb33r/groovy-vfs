package kroovyban.concept


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
    void testUnique() {
		def u1 = new User( login:"ysb33r", email:"y@b.example", name:"Schalk" )
		assert u1.validate()
		u1.save()
		
		def u2 = new User( login:"ysb33r", email:"y@b.example", name:"Schalk" )
		assert !u2.validate() : "Expected unique constraint to be violated"
		
		u2 = new User ( login:"ysb", email:"y@b.example", name:"Schalk" )
		assert u2.validate() : "Only login names should be unique"
    }
}
