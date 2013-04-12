A DSL for Groovy to wrap around the Apache VFS libraries

=====================
```
  import org.ysb33r.groovy.dsl.vfs.VFS

  def vfs = new VFS()
 
  // Simple copy operation 
  vfs.cp ftp://foo.example/myfile sftp://bar.example/yourfile
 
   // Utilising the DSL 
   vfs << {
   
     // Copy file from one site to anther using two different protocols
     cp http://first.example/myfile, sftp://second.example/yourfile
     
     // Not implemented yet - move file between two sites using different protocols
     mv sftp://second.example/yourfile, ftp://third.example/theirfile
     
     // Lists all files on a remote site
     ls http://first.example {
       println it.name
     }
      
     // Streams the output 
     println cat (http://first.example/myfile) .text 
     
   }  
```

