FROM openjdk:8
COPY build/classes/java/main/ /tmp
COPY build/output/lib /tmp/dependencies
WORKDIR /tmp
CMD java -cp "./dependencies/*:./" org.example.server.GrpcServer


