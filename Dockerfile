FROM openjdk:8-jdk-alpine as runtime
EXPOSE 8080
ENV APP_HOME /app
RUN mkdir $APP_HOME
WORKDIR $APP_HOME
COPY /server/target/*.jar app.jar
ENTRYPOINT exec java -jar -Dspring.profiles.active=$PROFILE app.jar
