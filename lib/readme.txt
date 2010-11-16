To generate md5 and sha1 files use maven install plugin as tool following instructions on http://maven.apache.org/plugins/maven-install-plugin/examples/installing-checksums.html.

The order for pom files was:
P:\Bluebell\05-Construccion\Bluebell\trunk\bluebell-richclient>
mvn install:install-file -DcreateChecksum=true -DgroupId=foo -DartifactId=foo -Dversion=foo -Dpackaging=pom -Dfile=trident-1.2-SNAPSHOT.pom

The order for jar files was:
P:\Bluebell\05-Construccion\Bluebell\trunk\bluebell-richclient>
mvn install:install-file -DcreateChecksum=true -DgroupId=foo -DartifactId=foo -Dversion=foo -Dpackaging=jar -Dfile=trident-1.2-SNAPSHOT.jar

The .md5 and .sha1 files are generated into ".m2/repository/foo/foo/foo/"

Samples:

call mvn install:install-file -DcreateChecksum=true -DgroupId=org.pushingpixels -DartifactId=substance-swingx -Dversion=6.0 -Dpackaging=jar -Dfile=substance-swingx-6.0.jar
call mvn install:install-file -DcreateChecksum=true -DgroupId=org.pushingpixels -DartifactId=substance-swingx -Dversion=6.0 -Dpackaging=jar -Dfile=substance-swingx-6.0-sources.jar -Dclassifier=sources
call mvn install:install-file -DcreateChecksum=true -DgroupId=org.pushingpixels -DartifactId=substance-swingx -Dversion=6.0 -Dpackaging=pom -Dfile=substance-swingx-6.0.pom
