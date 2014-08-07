@Grab('org.ysb33r.groovy:groovy-nfs:0.0.1-SNAPSHOT')

/*
import org.ysb33r.nfs.NfsClient

def client = new NfsClient()

def nfs = client.connect('192.168.1.10','/export')
*/

import org.ysb33r.nfs.v3.Nfs3
import org.ysb33r.rpc.Rpc
import org.ysb33r.nfs.cache.NfsBasicCache
import org.ysb33r.rpc.cred.CredUnix
import org.ysb33r.rpc.ip.ConnectSocket
import org.ysb33r.nfs.v3.FileAttributes3

//def conn = new ConnectSocket('127.0.0.1',2049,32768 + 512)
//conn.wait()
def rpc = new Rpc( new ConnectSocket('127.0.0.1',2049,32768 + 512), 100003, 3 )
def cache = new NfsBasicCache()
def cred = new CredUnix()
def fattr = new FileAttributes3()
def fh = new byte[32]
try {
def nfs = new Nfs3(rpc,fh,'/Users',fattr,cache,cred)
} catch (Exception e ) {
println e.printStackTrace()
}
////def nfs = new Nfs3()
////    Nfs3(Rpc rpc, byte[] fh, String path, FileAttributes3 fattr, NfsCache cache, CredUnix cred) {