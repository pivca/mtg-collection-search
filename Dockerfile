FROM node:22-bookworm-slim AS frontend-build
WORKDIR /workspace/mtgcs-fe-app

COPY mtgcs-fe-app/package*.json ./
RUN npm ci

COPY mtgcs-fe-app/ ./
RUN npm run build

FROM maven:3.9.9-eclipse-temurin-21-jammy AS backend-build
WORKDIR /workspace

COPY mtgcs-app/pom.xml mtgcs-app/pom.xml
RUN mvn -f mtgcs-app/pom.xml dependency:go-offline

COPY mtgcs-app/src mtgcs-app/src
COPY --from=frontend-build /workspace/mtgcs-fe-app/dist mtgcs-app/src/main/resources/static
RUN mvn -f mtgcs-app/pom.xml -DskipTests package

# Scraping uses plain HTTP + Jsoup, so the runtime only needs a JRE (no browser).
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

COPY --from=backend-build /workspace/mtgcs-app/target/mtgcs-app-*.jar app.jar

EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
