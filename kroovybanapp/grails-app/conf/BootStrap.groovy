import org.kroovyban.User
import org.kroovyban.UserAuthority
import org.kroovyban.Authority
import org.kroovyban.State
import org.kroovyban.SystemState
import org.kroovyban.WorkflowState
import grails.util.Environment               
import org.codehaus.groovy.grails.commons.*



class BootStrap {

	def springSecurityService

    def init = { servletContext ->
        def sysadminRole = 
            Authority.findByAuthority('ROLE_SYSADMIN') ?:
            new Authority(authority:'ROLE_SYSADMIN').save(flush:true,failOnError:true)
		
		// Depending on whether we are working with LDAP/OpenID or Local DB we must 
        // bootstrap a username to be an initial sysadmin
        def config = ConfigurationHolder.config
        def initUser = config.bootstrap.init.sysadmin
        
        if(Environment.current =~ /_(ldap|openid)$/)
        {
        }
        else
        {
            def u = 
                User.findByUsername(initUser) ?:
                new User( username : initUser, 
                          password : springSecurityService.encodePassword('password') , 
                          enabled : true ).save(flush:true,failOnError:true)
                          
                if(!u.authorities.contains(sysadminRole))
                {
                    UserAuthority.create(u, sysadminRole, true)
                }
        }

		
/*       
        def systemState = State.findByName('UNCONFIRMED') ?:
            new SystemState( name : 'UNCONFIRMED',effect : "INITIAL" ).save(flush:true,failOnError:true)

		systemState = State.findByName('COMPLETED') ?:
			new SystemState( name:'COMPLETED', effect : "FINAL" ).save(flush:true,failOnError:true)
		
        systemState = State.findByName('REJECTED') ?:
            new SystemState( name:'REJECTED', effect : "FINAL" ).save(flush:true,failOnError:true)
        
		systemState = State.findByName('PROMOTED') ?:
			new SystemState( name:'PROMOTED', effect : "PROMOTE" ).save(flush:true,failOnError:true)
			
		systemState = State.findByName('BATCH_PROMOTED') ?:
			new SystemState( name:'BATCH_PROMOTED', effect : "BATCH_PROMOTE" ).save(flush:true,failOnError:true)

		def newState = State.findByName('READY') ?:
			new WorkflowState( name:'READY', hasCompleted:false ).save(flush:true,failOnError:true)

		newState = State.findByName('SPEC') ?:
			new WorkflowState( name:'SPEC', hasCompleted:true ).save(flush:true,failOnError:true)

		newState = State.findByName('DEVTEST') ?:
			new WorkflowState( name:'DEVTEST', hasCompleted:true ).save(flush:true,failOnError:true)

		newState = State.findByName('VERIFY') ?:
			new WorkflowState( name:'VERIFY', hasCompleted:true ).save(flush:true,failOnError:true)

        newState = State.findByName('DEPLOY') ?:
            new WorkflowState( name:'DEPLOY', hasCompleted:true ).save(flush:true,failOnError:true)
*/			
}
    
    def destroy = 
    {
    }
}
