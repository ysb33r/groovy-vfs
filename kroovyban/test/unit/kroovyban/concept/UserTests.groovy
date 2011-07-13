package kroovyban.concept

import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.domain.DomainUnitTestMixin} for usage instructions
 */
@TestFor(User)
class UserTests {

    void testConstraints() {
		
		def goodUser = new User( 
			login:"ysbeer", 
			email:"yw-kroovyban-spam@af.org.za", 
			name:"Schalk W. Cronjé")
		mockForConstraintsTests(User, [ goodUser ])
		
		def u = new User()
		assert !u.validate() : "Empty User class is not valid"

		assert "nullable" == u.errors["login"] : "Expected nullable constraint to be violated"
		assert "nullable" == u.errors["email"] : "Expected nullable constraint to be violated"
		assert "nullable" == u.errors["name"] : "Expected nullable constraint to be violated"
		
		u = new User( login:"ysb33r", email:"yw-kroovyban-spam@af.org.za", name:"Schalk W. Cronjé")
		assert u.validate() : "Should have a valid class, if login, email and name is valid"
		assert u.enabled == true : "By default a new user should be enabled" 
		assert u.sysadmin == false : "By default a new user should not be a sysadmin"

		u = new User( login:"     ", email:"y@b.com", name:"Schalk"  )
		assert !u.validate() : "Expected a login with only blank space not to be valid"
		assert "blank" == u.errors["login"] : "Expected blank constraint to be violated"
	
		u = new User( login:"aa", email:"y@b.com", name:"Schalk"  )
		assert !u.validate() : "Expected a login with only two characters not to be valid"
		assert "minSize" == u.errors["login"] : "Expected minSize constraint to be violated"

		u = new User( login:" bb ", email:"y@b.com", name:"Schalk" )
		assert !u.validate() : "Expected a login surrounded by whitespace not to be valid"
		assert "validator" == u.errors["login"] : "Expected validator constraint to be violated"

		u = new User( login:"b b", email:"y@b.com", name:"Schalk" )
		assert !u.validate() : "Expected a login containg embedded whitespace not to be valid"
		assert "validator" == u.errors["login"], "Expected validator constraint to be violated"

		u = new User( login:" b\nb\t", email:"y@b.com", name:"Schalk" )
		assert !u.validate() : "Expected a login containg whitespace anywherenot to be valid"
		assert "validator" == u.errors["login"] : "Expected validator constraint to be violated"

		u = new User( login:"xxx", email:"y", name:"Schalk")
		assert !u.validate() : "Expected an non-valid email address to fail validation"
		assert "email" == u.errors["email"] : "Expected email constraint to be violated"

	}
}
