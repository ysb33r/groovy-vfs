package kroovybanapp

import static org.junit.Assert.*
import org.junit.*
import org.springframework.orm.hibernate3.HibernateSystemException
import grails.plugins.springsecurity.SpringSecurityService 
import org.codehaus.groovy.grails.commons.GrailsApplication

import org.kroovyban.Swimlane
import org.kroovyban.SwimlaneServiceClass
import org.kroovyban.ClassOfServiceDelivery

class SwimlaneClassOfServiceIntegrationTests {

    @Before
    void setUp() {
        // Setup logic here
    }

    @After
    void tearDown() {
        // Tear down logic here
    }

    @Test(expected=HibernateSystemException)
    void testUniqueness() {
	
		def sw = new Swimlane( name:"MySwimlane" ).save()
		def cos = new ClassOfServiceDelivery( name : "STANDARD", description:"Integration test COS" ).save()
		def swcos1 = new SwimlaneServiceClass( swimlane:sw, classOfService:cos ).save()		
		def swcos2 = new SwimlaneServiceClass( swimlane:sw, classOfService:cos ).save()
    }
}
