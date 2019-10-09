FROM openjdk:8-jdk-alpine as runtime
EXPOSE 8080
ENV APP_HOME /app
RUN mkdir $APP_HOME
WORKDIR $APP_HOME
COPY /target/*.jar app.jar
ENTRYPOINT exec java $JAVA_OPTS -jar -Dspring.profiles.active=$PROFILE app.jar $0 $@
