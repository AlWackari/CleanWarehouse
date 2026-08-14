FROM eclipse-temurin:21-jre
WORKDIR /app
COPY ./target/*-jar-with-dependencies.jar app.jar 
ENV MAGAT_ADMIN_KEY=secret123
CMD ["java", "-jar", "app.jar", "80"]