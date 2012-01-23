call svn export -r 25 http://recaptcha4j.googlecode.com/svn/trunk/recaptcha4j/ recaptcha4j-0.0.8

set SRC_DIR=recaptcha4j-0.0.8
copy ..\..\poms\modules\recaptcha\recaptcha4j-0.0.8.pom %SRC_DIR%\pom.xml
call mvn clean source:jar javadoc:jar --file %SRC_DIR%/pom.xml
