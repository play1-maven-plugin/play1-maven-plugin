Why so many files?

1. For building Play! framework and modules from sources using Maven I use:
 - pom.xml
 - assembly-framework.xml
 - assembly-framework-min.xml
 - assembly-distribution.xml
 - assembly-module.xml
 - assembly-module-min.xml

2. Because Play! framework and modules and not my own, I don't want to deploy poms
   from point 1. with all build details (because this is only my build, not the author's one).
   I deploy pom files without build details:
 - pom-dist.xml

3. When deploying mavenized framework and modules I use scripts from tools/scripts/deploy
   directory. They are slightly different from scripts from point 1. They use files:
 - pom-build-dist.xml
 - assembly-framework-build-dist.xml
 - assembly-framework-min.xml
 - assembly-module-build-dist.xml
 - assembly-module-min.xml
