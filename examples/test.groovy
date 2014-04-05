@Grapes([
//    @Grab( 'org.ysb33r.groovy:groovy-vfs:0.6-SNAPSHOT' ),
    @Grab( 'commons-net:commons-net:3.+' ), // If you want to use ftp 
    @Grab( 'commons-httpclient:commons-httpclient:3.1'), // If you want http/https
    @Grab( 'com.jcraft:jsch:0.1.48' ), // If you want sftp
    @Grab( 'jcifs:jcifs:1.3.17') // if you want smb/cifs
])
import org.ysb33r.groovy.dsl.vfs.VFS
import  org.apache.commons.logging.impl.SimpleLog

def sl = new SimpleLog('test')
sl.setLevel ( SimpleLog.LOG_LEVEL_ALL )

def vfs = new VFS( logger : sl )

vfs {
	//ls ('ftp://www.mirrorservice.org/sites/mageia.org/pub/mageia?vfs.ftp.passiveMode=true') {println it.name}
	// ls ('http://mirror.dacentec.com/mageia/') {println it.name}

	cp 'http://mirror.dacentec.com/mageia/paths.readme',new File ('test.download'), overwrite:true
}
