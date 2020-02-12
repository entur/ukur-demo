FROM openjdk:11-jre
WORKDIR /deployments
COPY target/ukur-demo-*-SNAPSHOT.jar nabu.jar
CMD java $JAVA_OPTIONS -jar ukur-demo.jar
