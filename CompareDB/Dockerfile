FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml mvnw ./
COPY .mvn ./.mvn
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src ./src
RUN ./mvnw -B clean package -DskipTests

FROM eclipse-temurin:21-jre

RUN apt-get update \
	&& apt-get install -y --no-install-recommends \
		libgtk-3-0 \
		libgl1 \
		libx11-6 \
		libxext6 \
		libxi6 \
		libxrender1 \
		libxtst6 \
	&& rm -rf /var/lib/apt/lists/*

WORKDIR /app/data

COPY --from=build /build/target/CompareDB-0.0.1-SNAPSHOT.jar /app/comparedb.jar

VOLUME ["/app/data"]

ENV DISPLAY=:0

ENTRYPOINT ["java", "-jar", "/app/comparedb.jar"]