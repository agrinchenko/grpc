package org.example.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.example.service.UserService;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.IOException;

/*
* To run from the CLI:  java -jar ./build/libs/basicGrpc-1.0-SNAPSHOT.jar
* */

public class GrpcServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(9091)
                .addService(new UserService())
                .addService(ProtoReflectionService.newInstance()) // Adding the Reflection service
                .build();
        server.start();
        System.out.println("Server started on port "+ server.getPort());
        server.awaitTermination();
    }
}
