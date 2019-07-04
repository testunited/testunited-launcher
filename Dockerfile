FROM openjdk:11
WORKDIR /app
ARG JAR_FILE
VOLUME ["/app/m2"]
RUN mkdir /app/test-jars
COPY startup.sh /app/startup.sh
COPY ./${JAR_FILE} /app/app.jar
RUN chmod +x /app/startup.sh
ENTRYPOINT ["/app/startup.sh"]