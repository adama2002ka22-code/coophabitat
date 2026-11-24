FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests \
      -Dmaven.wagon.http.retryHandler.count=10 \
      -Dmaven.wagon.http.pool=false \
      -Dmaven.wagon.rto=120000 \
      package

FROM eclipse-temurin:17-jre
RUN apt-get update && apt-get install -y --no-install-recommends fontconfig fonts-dejavu-core && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV JAVA_OPTS="-Xmx256m -XX:MaxMetaspaceSize=128m -Djava.awt.headless=true"
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]