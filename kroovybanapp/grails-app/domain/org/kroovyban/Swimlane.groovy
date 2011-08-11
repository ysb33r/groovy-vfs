package org.kroovyban

import org.kroovyban.ClassOfServiceDelivery

// I implemented Swimlane using a no-collections approach
// I got the idea from
// - https://mrpaulwoods.wordpress.com/2011/02/07/implementing-burt-beckwiths-gorm-performance-no-collections/
// - http://www.infoq.com/presentations/GORM-Performance

class Swimlane {

    String name
    String processUrl
    boolean enabled = true
    
/*
    workflow        : workflow_state:
                        state       : reference to a defined state
                        wiplimit    : nonNegativeInteger
                        next        : array of next workflow_state,required for all non-terminal states
                        promotion   : array of swimlanes that this state can promote to, only required for specific terminal states
*/

    static constraints = {
        name nullable:false, blank:false
        processUrl nullable:true, blank:false, url:true
    }

    def getServiceClasses() {
        SwimlaneServiceClass.findAllBySwimlane(this)
    }

    def getUserAuthorityName() {
            "ROLE_SWIMLANE_USER_${name}"
    }

    def getAdminAuthorityName() {
            "ROLE_SWIMLANE_ADMIN_${name}"
    }
	
    def getAdmins() {
		UserAuthority.findByAuthority( Authority.findByAuthority(getAdminAuthorityName()) ).collect({it.user})
    }

    def getUsers() {
		UserAuthority.findByAuthority( Authority.findByAuthority(getUserAuthorityName()) ).collect({it.user})
    }
}

