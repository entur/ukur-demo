FROM gcr.io/distroless/java21-debian13:nonroot@sha256:128bd878463537eef7771b50fecf68faace9089f654a2d233f1c4f344e6817d9
COPY target/ukur-demo-*.jar /ukur-demo.jar
EXPOSE 8080
CMD ["/ukur-demo.jar"]