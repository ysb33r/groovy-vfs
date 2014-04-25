// ============================================================================
// (C) Copyright Schalk W. Cronje 2014
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================
package org.ysb33r.groovy.vfs.app

import groovy.transform.*
import org.apache.commons.cli.*
import org.ysb33r.groovy.dsl.vfs.URI as vfsURI
import org.ysb33r.groovy.dsl.vfs.URIException

@TupleConstructor
class Cmdline {
     static final List<String> commands = ['cp','mv','mkdir','cat']

     PrintWriter errorWriter= null
     PrintWriter usageWriter= null
     String name

    @CompileStatic
    static def parseCmdline( final String name_, String[] args) {
        return new Cmdline(name : name_) .parse(args)
    }

    @CompileStatic
    def parse(String[] args) {
        Closure parser= findParser(args)

        if(parser==null) {
            errormsg('Bad format. Use --help for options.')
            return 1i
        }

        if(parser==scriptParser) {
            return parser(args[0])
        }

        if(parser == help || parser == version ) {
            parser()
            return 0i as Integer
        }

        return parser(args.drop(1))
    }

    Closure findParser(final String[] args) {
        if(args.size() == 0) {
            return null
        }

        if(commands.find {it == args[0]}) {
            return this."parse${args[0].capitalize()}"
        }

        if(args[0]=~ /^--script=/ && args.size() == 1) {
            return scriptParser
        }

        if(args[0]=='--help' || args[0]=='-h') {
            return help
        }

        if(args[0]=='--version' || args[0]=='-V') {
            return version
        }

        return null
    }

    @CompileStatic
    private  void errormsg(final Exception e) {
        errormsg(e.toString())
    }

    @CompileStatic
    private  void errormsg(final String s) {
        if(errorWriter) {
            errorWriter << s << "\n"
        } else {
            System.err.println s
        }
    }

    @CompileStatic
     List<vfsURI> buildURIs(final List args) {
        args.collect {
            try {
                return new vfsURI(it.toString())
            }
            catch(final URIException e) {
                return new vfsURI(new File(it.toString()))
            }
         }
    }

    private  Map parseCommand(
            final Map options,
            final String[] args,
            final String usage,
            final String header='',
            final String footer=''
    ) {
        CliBuilder cli
        if(usageWriter) {
            cli = new CliBuilder( 'usage':"${name} ${usage}", 'header':header, 'footer':footer, writer: usageWriter )
        } else {
            cli = new CliBuilder( 'usage':"${name} ${usage}", 'header':header, 'footer':footer )
        }
        cli.h(longOpt: 'help', 'Display command help and exit', required:false)
//        cli.V(longOpt: 'version', 'Display version and exit', required:false)
        cli._(longOpt: 'vfs-options','Comma-separated list of vfs.SCHEME.xyz options',required:false,valueSeparator:',')
        cli._(longOpt: 'logger', 'Turn logging on', required:false)
        options.each { keyname,o ->
            if(o.args) {
                cli."${keyname}"(
                    longOpt: o.longOpt,
                    required: false,
                    args:o.args,
                    argName:o.argName,
                    o.text
                )
            } else {
                cli."${keyname}"(longOpt: o.longOpt, required: false, o.text)
            }
        }
        OptionAccessor opts
        try {
            opts = cli.parse(args)
        } catch (final ParseException e) {
            errormsg(e)
            cli.usage()
            return null
        }
        if(!opts || opts.h) {
            cli.usage()
            return null
        }
        try {
            return ['cli': cli, 'opts': opts, uris: buildURIs(opts.arguments())]
        } catch (final URISyntaxException e) {
            errormsg(e)
            return null
        }
    }

    private  final def parseMkdir = { String[] args ->
        def parseResult = parseCommand(
            p : [ longOpt:'parents', text:'Make parent directories as needed' ],
            m : [ longOpt:'mode', args:1, argName:'MODE', text:'Set file mode (chmod-style) if VFS supports it (ignored)'],
            args,
            'mkdir [OPTIONS] uri1 ... '
        )
        if(parseResult?.opts) {
            try {
                Integer mode
                if(parseResult.opts.m) {
                    if(parseResult.opts.m.size() < 3 || parseResult.opts.m.size() >4) {
                        throw new NumberFormatException("Unix mode octal input string: \"${parseResult.opts.m}\"")
                    }
                    mode=Integer.parseInt(parseResult.opts.m,8)
                    if(mode>4095 || mode<0) {
                        throw new NumberFormatException("Unix mode octal input string: \"${parseResult.opts.m}\"")
                    }
                }
                return new Mkdir ( uris:parseResult.uris, intermediates : parseResult.opts.p, 'mode' : mode )
            } catch(final NumberFormatException e) {
                errormsg(e)
            }
        }
        return null
    }

    private  final def parseCat = { String[] args ->
    }

    private  final def parseCp = { String[] args ->
    }

    private  final def parseMv = { String[] args ->
    }

    private  final def help = { ->
        String output = """
Usage: [1] ${name} --help
       [2] ${name} --version
       [3] ${name} --script=ScriptFileName
       [4] ${name} COMMAND [COMMAND-OPTIONS] URIs...

[1] Display this help and exit
[2] Display program version and exit
[3] Runs a script of commands as in [4] (drop leading ${name})
[4] Execute a command with options, operating on one or more URIs
    COMMAND is any of ${commands}.
    Use '${name} COMMAND --help' for command-specific help.

"""
        if(usageWriter) {
            usageWriter << output << "\n"
        } else {
            println output
        }

        return 0i
    }

    private  final def version = {  ->
        String output = "${name} version ${this.class.package.implementationVersion}"
        if(usageWriter) {
            usageWriter << output << "\n"
        } else {
            println output
        }

        return 0i
    }

    private  final def scriptParser = { final String filename ->
        // Remove --script= from filename
        // Read file 1 by 1
        // Break line into string array
        // Call parse for each line
        // If any line does not return Cmd object, print errormsg and line no
        // otherwise add to list of objects to execute
        // If any errors return errorcode 2
        // otherwise return list of objects
    }
}