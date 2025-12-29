FROM eclipse-temurin:25-jdk-jammy
RUN apt-get update && apt-get install -y curl
VOLUME /tmp
ARG JAR_FILE=target/product-service-1.0.0.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]