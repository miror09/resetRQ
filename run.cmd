@echo off

setlocal

rem -----------------------------------------------------------------------
rem JRE location -- edit or remove (if already set) for your environment
rem -----------------------------------------------------------------------

set JAVA_HOME=d:\Java\jdk18

rem -----------------------------------------------------------------------
rem Folder locations -- may need to be changed for your environment
rem -----------------------------------------------------------------------

set APP_PATH=d:\eclipse\eclipse-workspace\resetRQ\
set CE_PATH=D:\IBM\FileNet\ContentEngine
set WEBLOGIC_PATH=d:\Oracle\Weblogic\wlserver

rem -----------------------------------------------------------------------
rem Set the CLASSPATH and options, then run
rem -----------------------------------------------------------------------

set APP_JAR=%APP_PATH%\resetRQ.jar
set JACE_JAR=%CE_PATH%\lib\Jace.jar
set LOG4J_JAR=d:\Oracle\NetBeans82\ide\modules\ext\log4j-1.2.15.jar
set WEBLOGIC_JAR=%WEBLOGIC_PATH%\server\lib\weblogic.jar
set JDBC=%WEBLOGIC_PATH%\server\lib\ojdbc6.jar

set CLASSPATH=%APP_JAR%;%JACE_JAR%;%LOG4J_JAR%;%WEBLOGIC_JAR%;%APP_PATH%;%JDBC%

set JAAS=-Djava.security.auth.login.config=%CE_PATH%\config\jaas.conf.WebLogic
set NAMING=-Djava.naming.factory.initial=weblogic.jndi.WLInitialContextFactory

"%JAVA_HOME%\bin\java" -cp "%CLASSPATH%" "%JAAS%" "%NAMING%" resetRQ.resetRQ %1
