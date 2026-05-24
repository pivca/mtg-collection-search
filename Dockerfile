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

FROM maven:3.9.9-eclipse-temurin-21-jammy AS runtime
WORKDIR /app

COPY mtgcs-app/pom.xml /tmp/mtgcs-pom.xml
RUN mvn -f /tmp/mtgcs-pom.xml -DskipTests org.codehaus.mojo:exec-maven-plugin:3.5.0:java \
      -Dexec.mainClass=com.microsoft.playwright.CLI \
      -Dexec.args="install --with-deps chromium" \
    && rm -rf /root/.m2 /tmp/mtgcs-pom.xml

COPY --from=backend-build /workspace/mtgcs-app/target/mtgcs-app-*.jar app.jar

EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
