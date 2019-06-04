cd target/build
mkdir answers

"%JAVA_HOME%\bin\jar.exe" uf %1 tool.properties
del tool.properties