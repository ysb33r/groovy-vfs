import org.kroovyban.User
import org.kroovyban.UserAuthority
import org.kroovyban.Authority
import grails.util.Environment               
import org.codehaus.groovy.grails.commons.*



class BootStrap {

	def SprintSecurityService
	
    def init = { servletContext ->
		if (Authority.count()==0)
		{
			def sysadminRole = new Authority(authority:'sysadmin').save(flush:true)
		}
		
		// Depending on whether we are working with LDAP/OpenID or Local DB we must 
        // bootstrap a username to be an initial sysadmin
        def config = ConfigurationHolder.config
        def initUser = config.bootstrap.init.sysadmin
        
        if(Environment.current =~ /_(ldap|openid)$/)
        {
        }
        else
        {
            def u = new User( username : initUser, password : 'password', enabled : true ).save(flush:true)
            UserAuthority.create(u, Authority.get(1), true)
        }
        
    }
    def destroy = {
    }
}
