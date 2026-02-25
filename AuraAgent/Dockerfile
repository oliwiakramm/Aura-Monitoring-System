
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN groupadd -r aura && useradd -r -g aura aura


COPY --from=build /app/target/aura-agent-1.0.0.jar app.jar

RUN chown aura:aura app.jar
USER aura

ENV AGENT_ID=agent-docker

ENTRYPOINT ["sh", "-c", "java -jar app.jar --agentId=${AGENT_ID}"]