FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN groupadd -r aura && useradd -r -g aura aura

# Używamy gwiazdki, żeby uniknąć problemów z wersją
COPY --from=build /app/target/*.jar app.jar

RUN chown aura:aura app.jar
USER aura
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
