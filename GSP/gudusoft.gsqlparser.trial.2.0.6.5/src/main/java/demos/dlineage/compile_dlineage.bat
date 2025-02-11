echo off

REM # This script/batch file is to compile the gsp demo. Users can make changes 
REM # to the demo and use this script/batch file to compile. JAVA_HOME environment variable
REM # should be set in the setenv.bat before running this script.

REM # Set the target directory where the compiled class should be copied.
set targetdir=build

REM # Change directory to gsp Demo Home directory
cd ..\..\..\..\..

REM # Run the setenv to set the environment variables.
call setenv\setenv.bat

   
    if NOT EXIST %JAVA_CMD% (
    echo. 
    echo ***************************
    echo JAVA_HOME is not set in the setenv\setenv.bat or not available
    echo Please set the JAVA_HOME. 
    echo eg. JAVA_HOME=C:\Program Files\Java\jdk1.7.0_80
    echo ***************************
    echo.
    cd src\main\java\demos\dlineage
    pause
    goto END
    )

	if NOT exist %targetdir% (
	    md %targetdir%
	)

REM # Compile the gsp demo
%JAVAC_CMD% -encoding UTF-8 -d %targetdir% -classpath %CLASSPATH% src\main\java\demos\dlineage\model\xml\*.java src\main\java\demos\dlineage\model\view\*.java src\main\java\demos\dlineage\model\metadata\*.java src\main\java\demos\dlineage\model\ddl\schema\*.java src\main\java\demos\dlineage\metadata\*.java src\main\java\demos\dlineage\util\*.java src\main\java\demos\dlineage\columnImpact\*.java src\main\java\demos\dlineage\dataflow\listener\*.java src\main\java\demos\dlineage\dataflow\model\*.java src\main\java\demos\dlineage\dataflow\model\json\*.java src\main\java\demos\dlineage\dataflow\model\xml\*.java src\main\java\demos\dlineage\sqlenv\*.java src\main\java\demos\dlineage\sqlenv\sqldep\*.java src\main\java\demos\dlineage\*.java

echo Completed.

REM # Change directory to the original directory
cd src\main\java\demos\dlineage

pause

:END

