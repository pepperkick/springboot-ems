FROM openjdk:8-jdk-alpine as runtime
EXPOSE 8999
ENV APP_HOME /app
RUN mkdir $APP_HOME
WORKDIR $APP_HOME
COPY /target/*.jar app.jar
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar $0 $@
