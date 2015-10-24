/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2015
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
//
// ============================================================================
// (C) Copyright Schalk W. Cronje 2013-2015
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================
//
package org.ysb33r.groovy.vfs.app

import groovy.transform.*
import org.apache.commons.cli.*
import org.ysb33r.groovy.dsl.vfs.URI as vfsURI
import org.ysb33r.groovy.dsl.vfs.URIException
import org.ysb33r.groovy.dsl.vfs.VFS

@TupleConstructor
class Cmdline {
    // TODO: ISSUE #22 - Add encrypt
    // TODO: ISSUE #23 - Add touch
    // TODO: ISSUE #24 - Add rm
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

        if(parser== schemes) {
            return parser()
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

        if(args[0]=='--list-schemes' ) {
            return schemes
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
            final Integer minURIs,
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
//        cli._(longOpt: 'vfs-options','Comma-separated list of vfs.SCHEME.xyz options',required:false,valueSeparator:',')
//        cli._(longOpt: 'logger', 'Turn logging on', required:false)
        options.each { keyname,o ->
            if(keyname == 'longOpts' ) {
                o.each { longName,v ->
                    if(o.args) {
                        cli._(longOpt: longName, args:v.args, optionalArg:v.optionalArg, required: false, v.text)
                    } else {
                        cli._(longOpt: longName, required: false, v.text)
                    }
                }
            } else {
                if (o.args) {
                    cli."${keyname}"(
                            longOpt: o.longOpt,
                            required: false,
                            args: o.args,
                            argName: o.argName,
                            o.text
                    )
                } else if (o.longOpt) {
                    cli."${keyname}"(longOpt: o.longOpt, required: false, o.text)
                } else {
                    cli."${keyname}"(required: false, o.text)
                }
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
        if(!opts || opts.h || opts.arguments().size() < minURIs) {
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
            args,1,
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
        def parseResult = parseCommand(
                E : [ longOpt:'show-ends',     text:'Print a $ at each line end' ],
                n : [ longOpt:'number',        text:'Number output lines' ],
                b : [ longOpt:'number-nonblank',text:'Number non-blank lines (overrides -n)' ],
                s : [ longOpt:'squeeze-blank', text:'Suppress repeated empty output lines'],
                T : [ longOpt:'show-tabs',     text:'Display TAB characters as ^I'],
                A : [ longOpt:'show-all',      text:'Equivalent of -vET'],
                v : [ longOpt:'show-nonprinting',text:'Use ^ and M- notation, except for LFD and TAB'],
                e : [ text:'Equivalent of -vE'],
                t : [ text:'Equivalent of -vT'],
                u : [ text:'Compatibility switch - ignored'],
                args,1,
                'cat [OPTIONS] uri1 ... '
        )
        if(parseResult?.opts) {
            return new Cat (
                uris:parseResult.uris,
                numberNonEmptyLines        : parseResult.opts.b,
                showEndOfLines             : parseResult.opts.E || parseResult.opts.A || parseResult.opts.e,
                numberLines                : parseResult.opts.n,
                suppressRepeatedEmptyLines : parseResult.opts.s,
                showTabs                   : parseResult.opts.T || parseResult.opts.A || parseResult.opts.t,
                showNonPrinting            : parseResult.opts.v || parseResult.opts.A || parseResult.opts.e || parseResult.opts.t
            )
        }
        return null
    }

    private  final def parseMv = { String[] args ->
        def parseResult = parseCommand(
                d : [ longOpt:'force',       text:'Do not prompt before overwriting' ],
                i : [ longOpt:'interactive', text:'Prompt before overwriting' ],
                n : [ longOpt:'no-clobber',  text:'Do not overwrite existing file' ],
                u : [ longOpt:'update',      text:'Move only when source is newer that destination or latter is missing' ],
                v : [ longOpt:'verbose',     text:'Explain what is being done' ],
                T : [ longOpt:'no-target-directory', text:'Treat destination as file'],
                S : [ longOpt:'suffix', args:1, argName:'SUFFIX', text:'Override the usual backup suffix' ],
                t : [ longOpt:'target-directory', args:1, argName:'DIR', text:'Move all srcURIs to destination folder URI' ],
                b : [ text:'Like --backup=simple'],
                longOpts : [
                    backup : [ args:1, argName:'CONTROL', optionalArg:true, text:'Backup behaviour. Optional arg:'],
                    'strip-trailing-slashes' : [ text: 'Remove any trailing slashes from each srcURI'],
                    parents : [ text : 'Make parent directories as needed']
                ],
                args,1,
                'mv [OPTIONS] srcUri... destUri ','mv -t destUri srcUri...',
                '''If you specify more than one of -i, -f, -n, only the final one takes effect.
If you specify -T then only two URIs are allowed.'''
        )
        if(parseResult?.opts) {
           if(parseResult.opts.T && parseResult.uris.size() !=2 ) {
               errormsg('Only two URIs are allowed with -T')
               return null
           }
           def overwriteAction=true
           switch( (args.reverse() as String[]).find { it in ['-n','-i','-f','--force','--no-clobber','--interactive'] } ) {
                case '-n':
                case '--no-clobber:':
                   overwriteAction=false
                   break
                case '-i':
                case '--interactive':
                   overwriteAction = { from,to ->
                       print "Overwrite ${to}? "
                       System.console().readLine().toUpperCase().startsWith('Y')
                   }

           }

           org.ysb33r.groovy.dsl.vfs.URI dest = null
           if(parseResult.opts.t) {
               try {
                   dest = buildURIs([parseResult.opts.t])[0]
               } catch (final URISyntaxException e) {
                   errormsg(e)
                   return null
               }
           } else {
               if(parseResult.uris.size() < 2) {
                   errormsg "Not enough URIs specified - at least two required."
                   return null
               }
               dest = parseResult.uris.pop()
           }

           if(parseResult.opts.'strip-trailing-slashes') {
               parseResult.uris = parseResult.uris.collect {
                    def uriStr=it.toString()
                    if(uriStr.endsWith('/')) {
                        it=new vfsURI(uriStr.substring(0,uriStr.size()-1))
                    }
                    return it
                }
           }
           return new Mv(
                   sources :parseResult.uris,
                   destination : dest,
                   targetIsFile : parseResult.opts.T,
                   backupSuffix : parseResult.opts.S ?: (System.getenv('SIMPLE_BACKUP_SUFFIX') ?: '~'),
                   update : parseResult.opts.u,
                   overwrite : overwriteAction,
                   verbose : parseResult.opts.v,
                   intermediates: parseResult.opts.parents,
                   interactive: (overwriteAction instanceof Closure)
           )
        }
        return null
//    The version control method may be selected via the --backup option or through
//    the VERSION_CONTROL environment variable.  Here are the values:
//
//    none, off
//    never make backups (even if --backup is given)
//
//    numbered, t
//    make numbered backups
//    ﻿       existing, nil
//    numbered if numbered backups exist, simple otherwise
//
//    simple, never
//    always make simple backups
        // Need to read env variables SIMPLE_BACKUP_SUFFIX, VERSION_CONTROL
    }

    private  final def parseCp = { String[] args ->
        def parseResult = parseCommand(
                d : [ longOpt:'force',       text:'Do not prompt before overwriting' ],
                i : [ longOpt:'interactive', text:'Prompt before overwriting' ],
                n : [ longOpt:'no-clobber',  text:'Do not overwrite existing file' ],
                u : [ longOpt:'update',      text:'Move only when source is newer that destination or latter is missing' ],
                v : [ longOpt:'verbose',     text:'Explain what is being done' ],
                T : [ longOpt:'no-target-directory', text:'Treat destination as file'],
                S : [ longOpt:'suffix', args:1, argName:'SUFFIX', text:'Override the usual backup suffix' ],
                t : [ longOpt:'target-directory', args:1, argName:'DIR', text:'Copy all srcURIs to destination folder URI' ],
                r : [ longOpt:'recursive',   text:'Copy directories recursively'],
                b : [ text:'Like --backup=simple'],
                R : [ text:'Copy directories recursively'],
                longOpts : [
                        backup : [ args:1, argName:'CONTROL', optionalArg:true, text:'Backup behaviour. Optional arg:'],
                        'strip-trailing-slashes' : [ text: 'Remove any trailing slashes from each srcURI'],
                        'remove-destination': [ text: 'Remove each existing destination file before attempting to open it']
                ],
                args,1,
                'cp [OPTIONS] srcUri... destUri ','cp -t destUri srcUri...',
                '''If you specify more than one of -i, -f, -n, only the final one takes effect.
If you specify -T then only two URIs are allowed.'''
        )
        if(parseResult?.opts) {
            if(parseResult.opts.T && parseResult.uris.size() !=2 ) {
                errormsg('Only two URIs are allowed with -T')
                return null
            }
            def overwriteAction=true
            switch( (args.reverse() as String[]).find { it in ['-n','-i','-f','--force','--no-clobber','--interactive'] } ) {
                case '-n':
                case '--no-clobber:':
                    overwriteAction=false
                    break
                case '-i':
                case '--interactive':
                    overwriteAction = { from,to ->
                        print "Overwrite ${to}? "
                        System.console().readLine().toUpperCase().startsWith('Y')
                    }

            }

            // TODO: ISSUE #26 - parseResults.opts.'remove-destination; needs updating to correct behaviour.

            org.ysb33r.groovy.dsl.vfs.URI dest = null
            if(parseResult.opts.t) {
                try {
                    dest = buildURIs([parseResult.opts.t])[0]
                } catch (final URISyntaxException e) {
                    errormsg(e)
                    return null
                }
            } else {
                if(parseResult.uris.size() < 2) {
                    errormsg "Not enough URIs specified - at least two required."
                    return null
                }
                dest = parseResult.uris.pop()
            }

            if(parseResult.opts.'strip-trailing-slashes') {
                parseResult.uris = parseResult.uris.collect {
                    def uriStr=it.toString()
                    if(uriStr.endsWith('/')) {
                        it=new vfsURI(uriStr.substring(0,uriStr.size()-1))
                    }
                    return it
                }
            }

            return new Cp(
                    sources :parseResult.uris,
                    destination : dest,
                    targetIsFile : parseResult.opts.T,
                    //backupSuffix : parseResult.opts.S ?: (System.getenv('SIMPLE_BACKUP_SUFFIX') ?: '~'),
                    update : parseResult.opts.u,
                    overwrite : overwriteAction,
                    verbose : parseResult.opts.v,
                    recursive : parseResult.opts.r || parseResult.opts.R,
                    interactive: (overwriteAction instanceof Closure)
            )
        }
        return null
//    The version control method may be selected via the --backup option or through
//    the VERSION_CONTROL environment variable.  Here are the values:
//
//    none, off
//    never make backups (even if --backup is given)
//
//    numbered, t
//    make numbered backups
//    ﻿       existing, nil
//    numbered if numbered backups exist, simple otherwise
//
//    simple, never
//    always make simple backups
        // Need to read env variables SIMPLE_BACKUP_SUFFIX, VERSION_CONTROL
    }

    private  final def help = { ->
        String output = """
Usage: [1] ${name} --help
       [2] ${name} --version
       [3] ${name} --script=ScriptFileName
       [4] ${name} --list-schemes
       [5] ${name} COMMAND [COMMAND-OPTIONS] URIs...

[1] Display this help and exit
[2] Display program version and exit
[3] Runs a script of commands as in [5] (drop leading ${name})
[4] Lists current supported schemes
[5] Execute a command with options, operating on one or more URIs
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

    private final def schemes = {  ->
        return [
            run : { VFS vfs ->
                vfs.fsMgr.schemes.sort().each {
                    String output="  ${it}"
                    if(usageWriter) {
                        usageWriter << output << "\n"
                    } else {
                        println output
                    }
                }
                0i
            },
            isInteractive : { -> false }
        ] as Cmd
v    }

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