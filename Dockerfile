FROM openjdk:17-alpine
LABEL maintainer=maksim
WORKDIR /app
COPY libs libs/
COPY resources resources/
COPY classes classes/
ENTRYPOINT ["java", "-cp", "/app/resources:/app/classes:/app/libs/*", "com.example.demotest.DemotestApplication"]
EXPOSE 8080
