# Utilisez une image avec Java 17
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Définir l'encodage UTF-8 explicitement
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Ajoutez -Dfile.encoding=UTF-8 et vérifiez les fichiers
RUN apt-get update && apt-get install -y file
RUN find /app/src -name "*.properties" -exec file {} \;

# Compilez avec encodage UTF-8
RUN mvn clean package -DskipTests -Dfile.encoding=UTF-8

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar"]