FROM openjdk:16
ARG JAR_FILE=lib/flowkind-core-review-service-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
#EXPOSE 8080

#FROM openjdk:16 as builder
#WORKDIR extracted
#ADD lib/flowkind-core-review-service-0.0.1-SNAPSHOT.jar app.jar
#RUN java -Djarmode=layertools -jar app.jar extract
#
#FROM openjdk:16
#WORKDIR application
#COPY --from=builder extracted/dependencies/ ./
#COPY --from=builder extracted/spring-boot-loader/ ./
#COPY --from=builder extracted/snapshot-dependencies/ ./
#COPY --from=builder extracted/application/ ./
#
##EXPOSE 8080
#
#ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]