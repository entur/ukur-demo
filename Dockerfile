FROM gcr.io/distroless/java21-debian12:nonroot
COPY target/ukur-demo-*.jar /ukur-demo.jar
EXPOSE 8080
CMD ["/ukur-demo.jar"]