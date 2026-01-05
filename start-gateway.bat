@echo off
echo ========================================
echo   DEMARRAGE GRAPHQL GATEWAY
echo ========================================
echo.

echo [1/3] Verification de Java...
java -version
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Java n'est pas installe ou pas dans le PATH
    pause
    exit /b 1
)

echo.
echo [2/3] Compilation du projet...
call mvnw.cmd clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: La compilation a echoue
    pause
    exit /b 1
)

echo.
echo [3/3] Demarrage du GraphQL Gateway sur le port 8090...
echo.
echo URL GraphQL: http://localhost:8090/graphql
echo GraphiQL UI: http://localhost:8090/graphiql
echo.
java -jar target\graphql-gateway-0.0.1-SNAPSHOT.jar
