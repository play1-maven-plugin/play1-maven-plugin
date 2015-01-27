call git clone --depth 1 -n -v git://github.com/swaldman/c3p0.git src
cd src
call git checkout -f c3p0-0.9.5
cd ..
copy ..\..\..\poms\play\c3p0-oracle-thin-extras-0.9.5.pom-build src\dbms\oracle-thin\pom.xml

