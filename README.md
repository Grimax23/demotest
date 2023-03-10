### 1. Сheck java version and $JAVA_HOME Required java 17!:
echo $JAVA_HOME

### 2. Build and run without docker with Spring Boot Gradle plugin :
./gradlew bootrun

### 3. Build and run in Docker with Spring Boot Gradle Docker plugin
./gradlew startContainer
 
### 4. Run with Docker CLI
docker build -t demotest:demo ./build/docker/
docker run -d -p 8080:8080 -t demotest:demo

### 5. Test Throttler from one ip (.../get): 
http://localhost:8080/get

### 6. Test Throttler from different ip (.../testIpThrottler/{ip}):
http://localhost:8080/testIpThrottler/0.0.0.1

### Default configuration in application.yml:
throttling:
  limit: 2 
  durationInMinutes: 1