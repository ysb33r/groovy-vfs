#!/usr/bin/env bash
#
# ============================================================================
# (C) Copyright Schalk W. Cronje 2013-2015
#
# This software is licensed under the Apache License 2.0
# See http://www.apache.org/licenses/LICENSE-2.0 for license details
#
# Unless required by applicable law or agreed to in writing, software distributed under the License is
# distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and limitations under the License.
#
# ============================================================================
#

srcd="$(dirname $0)/test-files"
destd="${srcd}/cat-expected"
linux=$(uname -s | grep -i Linux)

cat -n "${srcd}/file1.txt" > "${destd}/file1-numbered.txt"
cat -e "${srcd}/file1.txt" > "${destd}/file1-with-eol-markers.txt"
cat -b "${srcd}/file2.txt" > "${destd}/file2-numbered-notblanks.txt"
cat -b "${srcd}/file4.txt" > "${destd}/file4-numbered-notblanks.txt"
cat -t "${srcd}/file5.txt" > "${destd}/file5-show-tabs.txt"
# BSD cat drops the last line if empty
#if [[ -z $linux ]] ; then
#    echo >> "${destd}/file4-numbered-notblanks.txt"
#fi

cat -s "${srcd}/file3.txt" > "${destd}/file3-suppressed-repeating-blanks.txt"
cat -s -n "${srcd}/file3.txt" > "${destd}/file3-suppressed-repeating-blanks-numbered.txt"
cat -s -b "${srcd}/file3.txt" > "${destd}/file3-suppressed-repeating-blanks-nonblank-numbered.txt"

cat -v "${srcd}/file6.dat" > "${destd}/file6-show-non-printing.txt"
cat -n "${srcd}/file1.txt" "${srcd}/file2.txt" > "${destd}/file1-file2-numbered.txt"
