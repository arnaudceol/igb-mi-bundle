igbversion=8.1.11


mvn deploy:deploy-file -Dfile=genoviz.jar -DgroupId=org.affymetrix -DartifactId=genoviz-util -Dversion=$igbversion -Dpackaging=jar -DrepositoryId=iit-cru -Durl=http://cru.genomics.iit.it/nexus/content/repositories/thirdparty/ 
mvn deploy:deploy-file -Dfile=genometry.jar -DgroupId=org.affymetrix -DartifactId=genoviz-genometry -Dversion=$igbversion -Dpackaging=jar -DrepositoryId=iit-cru  -Durl=http://cru.genomics.iit.it/nexus/content/repositories/thirdparty/ 
mvn deploy:deploy-file -Dfile=igb.jar -DgroupId=org.affymetrix -DartifactId=genoviz-igb -Dversion=$igbversion -Dpackaging=jar -DrepositoryId=iit-cru  -Durl=http://cru.genomics.iit.it/nexus/content/repositories/thirdparty/ 
mvn deploy:deploy-file -Dfile=igb_service.jar -DgroupId=org.affymetrix -DartifactId=genoviz-igb-service -Dversion=$igbversion -Dpackaging=jar -DrepositoryId=iit-cru  -Durl=http://cru.genomics.iit.it/nexus/content/repositories/thirdparty/ 
mvn deploy:deploy-file -Dfile=window_service.jar -DgroupId=org.affymetrix -DartifactId=genoviz-window -Dversion=$igbversion -Dpackaging=jar -DrepositoryId=iit-cru -Durl=http://cru.genomics.iit.it/nexus/content/repositories/thirdparty/ 
mvn deploy:deploy-file -Dfile=common.jar -DgroupId=org.affymetrix -DartifactId=genoviz-common -Dversion=$igbversion -Dpackaging=jar -DrepositoryId=iit-cru  -Durl=http://cru.genomics.iit.it/nexus/content/repositories/thirdparty/



jmolversion=14.2.2
mvn deploy:deploy-file -Dfile=JmolLib.jar -DgroupId=org.jmol -DartifactId=jmol-lib -Dversion=$jmolversion -Dpackaging=jar -DrepositoryId=iit-cru -Durl=http://cru.genomics.iit.it/nexus/content/repositories/thirdparty/ 
mvn deploy:deploy-file -Dfile=Jmol.jar -DgroupId=org.jmol -DartifactId=jmol -Dversion=$jmolversion -Dpackaging=jar -DrepositoryId=iit-cru -Durl=http://cru.genomics.iit.it/nexus/content/repositories/thirdparty/
