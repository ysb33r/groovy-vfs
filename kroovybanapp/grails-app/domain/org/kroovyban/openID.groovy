package org.kroovyban



class openID {

	String url

	static belongsTo = [user: User]

	static constraints = {
		url unique: true
	}
}
