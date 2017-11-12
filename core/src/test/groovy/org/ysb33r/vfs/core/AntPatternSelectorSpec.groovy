/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2017
 *
 * This software is licensed under the Apache License 2.0
 * See http://www.apache.org/licenses/LICENSE-2.0 for license details
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 * ============================================================================
 */
package org.ysb33r.vfs.core

import spock.lang.Specification
import spock.lang.Unroll


class AntPatternSelectorSpec extends Specification {

    static final def structure = [
        'file1.txt',
        'file2.txt',
        'subdirA/file3.txt',
        'subdirA/file4.txt',
        'subdirA/subdirB/file5.txt',
        'subdirA/subdirB/file6.txt',
        'subdirA/subdirB/file777.txt'
    ]

    static final def testsets = [

        'Select single file in a folder' : [
            filter : {
                include 'file1.txt'
            },
            included : [ 'file1.txt' ]
        ],

        'Select files in a single folder by ?-wildcard' : [
            filter : {
                include 'file?.txt'
            },
            included : [ 'file1.txt', 'file2.txt' ]
        ],

        'Select files recursively by ?-wildcard' : [
            filter : {
                include '**/file?.txt'
            },
            included : [ 'file1.txt', 'file2.txt', 'subdirA/file3.txt',
                         'subdirA/file4.txt',
                         'subdirA/subdirB/file5.txt',
                         'subdirA/subdirB/file6.txt',
            ]
        ],

        'Select files recursively by *-wildcard' : [
            filter : {
                include '**/file*.txt'
            },
            included : [ 'file1.txt', 'file2.txt', 'subdirA/file3.txt',
                         'subdirA/file4.txt',
                         'subdirA/subdirB/file5.txt',
                         'subdirA/subdirB/file6.txt',
                         'subdirA/subdirB/file777.txt'
            ]
        ],

        'Select everything recursively' : [
            filter : {
                include '**'
            },
            included : structure
        ],

        'Select files recursively, excluding by single ?-wildcard' : [
            filter : {
                include '**'
                exclude '**/file?.txt'

            },
            included : [
                'subdirA/subdirB/file777.txt',
            ]
        ],

        'Select only top-level' : [
            filter : {
                include '*'
            },
            included : [
                'file1.txt',
                'file2.txt'
            ]
        ],

        'Recusrively select only files that end in a folder + *-wildcard' : [
            filter : {
                include '**/subdirB/*'
            },
            included : [
                'subdirA/subdirB/file5.txt',
                'subdirA/subdirB/file6.txt',
                'subdirA/subdirB/file777.txt'
            ]
        ],
    ]

    static final def testNames = testsets.keySet() as List

    AntPatternSelector aps = new AntPatternSelector()

    @Unroll
    def "AntPattern Selector Internals: #testName"() {
        // This is a test of a specifc internal method. It will include/exclude
        // purely based upon pattern matching. It cannot check whether a specific
        // match is a folder or not.
        given:
        def c1 = testsets[testName].filter.clone()
        c1.delegate = aps
        c1.resolveStrategy = Closure.DELEGATE_ONLY
        c1.call()

        expect:
        testsets[testName].included == structure.findAll {
            aps.allowed(it)
        }

        where:
        testName << testNames
    }
}