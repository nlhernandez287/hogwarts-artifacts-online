FROM eclipse-temurin:17-alpine as builder
WORKDIR build
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre as layer
WORKDIR layer
ARG JAR_FILE=/build/target/*.jar
COPY --from=builder ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:17-jre
WORKDIR application
COPY --from=layer layer/dependencies/ ./
COPY --from=layer layer/spring-boot-loader/ ./
COPY --from=layer layer/snapshot-dependencies/ ./
COPY --from=layer layer/application/ ./
ENV spring.profiles.active=dev
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]