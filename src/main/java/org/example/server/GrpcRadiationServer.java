package org.example.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import org.example.service.RadiationService;

import java.io.IOException;

/* * To run from the CLI:  java -jar ./build/libs/basicGrpc-1.0-SNAPSHOT.jar
* */

public class GrpcRadiationServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        RadiationService radiationService = new RadiationService();
//        radiationService.initialize("/Users/fcmbp/Documents/dev/java/api/src/main/java/org/example/config");
        radiationService.initialize("./src/main/java/org/example/config");
        radiationService.connectToUsbDevice();
        Server server = ServerBuilder.forPort(9091)
                .addService(radiationService)
                .addService(ProtoReflectionService.newInstance()) // Adding the Reflection service
                .build();

        server.start();
        System.out.println("Radiation Server started on port "+ server.getPort());
        server.awaitTermination();
    }
}