# Usamos una imagen base con Java 23 y Maven para compilar
FROM eclipse-temurin:23-jdk AS build

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos el pom.xml y descargamos dependencias primero (cache de Docker)
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el resto del código
COPY src ./src

# Compilamos y empaquetamos la app
RUN mvn clean package -DskipTests

# ---------------------------------------------------
# Stage final: contenedor más liviano solo con Java
FROM eclipse-temurin:23-jdk

WORKDIR /app

# Copiamos el jar compilado desde el stage build
COPY --from=build /app/target/backendSB-0.0.1-SNAPSHOT.jar ./backendSB.jar

# Puerto que expondrá el contenedor
EXPOSE 8080

# Comando para ejecutar la app
CMD ["java", "-jar", "backendSB.jar"]
