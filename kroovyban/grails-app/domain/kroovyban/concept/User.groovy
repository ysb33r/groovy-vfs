package kroovyban.concept

class User {

	String login
	String name
	String email
	Boolean enabled = true
	Boolean sysadmin = false
	
    static constraints = {
		login(
			blank:false,unique:true,minSize:3,
			validator : { return !(it =~ '\\s') }
		)
		name(blank:false,minSize:3)
		email(email:true,blank:false)
    }
}
