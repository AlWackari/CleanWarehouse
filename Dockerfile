FROM eclipse-temurin:21-jre
WORKDIR /app
COPY ./target/*-jar-with-dependencies.jar app.jar 
CMD ["java", "-jar", "app.jar", "80"]