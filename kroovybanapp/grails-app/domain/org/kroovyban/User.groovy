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

    // Required for OpenID
    //static hasMany = [openIds: openID]

	static mapping = {
		password column: '`password`'
	}

	Set<Authority> getAuthorities() {
		UserAuthority.findAllByUser(this).collect { it.authority } as Set
	}
}
