package org.kroovyban

import org.apache.commons.lang.builder.HashCodeBuilder

class UserAuthority implements Serializable {

	User user
	Authority authority

	boolean equals(other) {
		if (!(other instanceof UserAuthority)) {
			return false
		}

		other.user?.id == user?.id &&
			other.authority?.id == authority?.id
	}

	int hashCode() {
		def builder = new HashCodeBuilder()
		if (user) builder.append(user.id)
		if (authority) builder.append(authority.id)
		builder.toHashCode()
	}

	static UserAuthority get(long userId, long authorityId) {
		find 'from UserAuthority where user.id=:userId and authority.id=:authorityId',
			[userId: userId, authorityId: authorityId]
	}

	static UserAuthority create(User user, Authority authority, boolean flush = false) {
		new UserAuthority(user: user, authority: authority).save(flush: flush, insert: true)
	}

	static boolean remove(User user, Authority authority, boolean flush = false) {
		UserAuthority instance = UserAuthority.findByUserAndAuthority(user, authority)
		instance ? instance.delete(flush: flush) : false
	}

	static void removeAll(User user) {
		executeUpdate 'DELETE FROM UserAuthority WHERE user=:user', [user: user]
	}

	static void removeAll(Authority authority) {
		executeUpdate 'DELETE FROM UserAuthority WHERE authority=:authority', [authority: authority]
	}

	static mapping = {
		id composite: ['authority', 'user']
		version false
	}
}
