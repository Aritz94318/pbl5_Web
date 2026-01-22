FROM tomcat:10.1-jdk17

# Crear usuario no-root
RUN useradd -m pinkalert

# Directorio de datos de la aplicación
RUN mkdir -p /app/data \
    && chown -R pinkalert:pinkalert /app/data \
    && chmod 750 /app/data

# Tomcat
RUN chown -R pinkalert:pinkalert /usr/local/tomcat

USER pinkalert

COPY target/crud_rest_db-0.0.1-SNAPSHOT.war \
     /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
