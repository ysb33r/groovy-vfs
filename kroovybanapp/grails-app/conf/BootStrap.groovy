import org.kroovyban.Authority
                     

class BootStrap {

	def SprintSecurityService
	
    def init = { servletContext ->
		if (Authority.count()==0)
		{
			def sysadminRole = new Authority(authority:'sysadmin').save(flush:true)
		}
    }
    def destroy = {
    }
}
