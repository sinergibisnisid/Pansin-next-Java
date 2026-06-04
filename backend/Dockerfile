FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -e -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -e -ntp clean package -DskipTests

FROM eclipse-temurin:21-jre
RUN useradd -ms /bin/bash pansin
WORKDIR /app
COPY --from=build /workspace/target/pansin-access.jar app.jar
RUN mkdir -p /app/storage/snapshots /app/storage/reports && \
    chown -R pansin:pansin /app
USER pansin
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseG1GC -Xms512m -Xmx1024m -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
