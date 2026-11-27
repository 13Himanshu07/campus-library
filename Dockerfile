FROM tomcat:10.1-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY target/campus-library.war /usr/local/tomcat/webapps/ROOT.war
COPY deployment/oracle/entrypoint.sh /usr/local/bin/campus-library-entrypoint.sh
RUN chmod 0755 /usr/local/bin/campus-library-entrypoint.sh
ENTRYPOINT ["/usr/local/bin/campus-library-entrypoint.sh"]
CMD ["catalina.sh", "run"]
EXPOSE 8080
