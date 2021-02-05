FROM openjdk:11-jre

RUN addgroup appuser && adduser --disabled-password appuser --ingroup appuser

WORKDIR /deployments
COPY target/ukur-demo-*.jar ukur-demo.jar

RUN chown -R appuser:appuser /deployments
USER appuser

CMD java $JAVA_OPTIONS -jar ukur-demo.jar
