/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2015
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * //
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * //
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * ============================================================================
 */
// ============================================================================
// (C) Copyright Schalk W. Cronje 2014
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
// ============================================================================

package org.ysb33r.groovy.vfsplugin.smb

import spock.lang.*
import org.apache.commons.vfs2.FileType

class SmbFileNameSpec extends Specification {
    def "Constructing an SmbFileName should set all appropriate properties"() {
        given:
            def sfn = new SmbFileName(
                'foo',
                'localhost',
                1139,
                'ysb33r',
                'ThereIsNoPassword',
                'TEST',
                'ShareAndShareAlike',
                '/Foo/Bar',
                FileType.IMAGINARY
            )

        expect:
            sfn.baseName        == 'Bar'
            sfn.parent.baseName == 'Foo'
            sfn.path            == '/Foo/Bar'
            sfn.scheme          == 'foo'
            sfn.rootURI         == 'foo://TEST\\ysb33r:ThereIsNoPassword@localhost:1139/ShareAndShareAlike/'
    }
}