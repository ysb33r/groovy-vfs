package org.kroovyban

class User {

	String username
	String password
	boolean enabled
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired

	static constraints = {
		username blank: false, unique: true
		password blank: false
	}

	static mapping = {
		password column: '`password`'
	}

        static User create( String name, String pw = 'x',boolean flush = false )
        {
            new User( username:name, password: pw ).save (flush : flush)
        }
        
	Set<Authority> getAuthorities() {
		UserAuthority.findAllByUser(this).collect { it.authority } as Set
	}
}
