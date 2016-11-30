if exist "%HOMEPATH%\OWASP ZAP\.ZAP_JVM.properties" (
	set /p jvmopts=< "%HOMEPATH%\OWASP ZAP\.ZAP_JVM.properties"
) else (
	set jvmopts=-Xmx2048m
)

java %jvmopts% -jar zap-dev.jar %*
