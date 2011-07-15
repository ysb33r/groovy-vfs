import org.kroovyban.User
import org.kroovyban.UserAuthority
import org.kroovyban.Authority
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
        
    }
    
    def destroy = 
    {
    }
}
