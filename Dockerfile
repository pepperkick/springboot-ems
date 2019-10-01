# Builder Image
FROM maven:3-jdk-8 as builder

# Build
RUN mkdir -p /build
WORKDIR /build
COPY pom.xml /build
RUN mvn -b  dependency:resolve dependency:resolve-plugins
COPY src /build/src
RUN mvn package

# Runtime Image
FROM openjdk:8-jdk-alpine as runtime
LABEL maintainer="PepperKick"
EXPOSE 8080
ENV APP_HOME /app
RUN mkdir $APP_HOME
WORKDIR $APP_HOME
COPY --from=builder /build/target/*.jar app.jar
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar $0 $@

