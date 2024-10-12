package org.example.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.grpc.userGrpc;

/*
* Alternatively the client can be tested using `grpcurl`
* CLI like so:
*  grpcurl -plaintext -d '{"username":"test", "password":"test"}' localhost:9091 user.login
*
* To list available services run:
*    grpcurl -plaintext localhost:9091 list
*    grpcurl -plaintext localhost:9091 list user.login
*
* To run JAR from CLI do:
* java -cp ./build/libs/basicGrpc-1.0-SNAPSHOT.jar  org.example.client.GrpcClient
* * */

public class GrpcClient {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9091)
                .usePlaintext(true)
                .build();

        userGrpc.userBlockingStub stub = userGrpc.newBlockingStub(channel);
        org.example.grpc.User.LoginRequest loginRequest = org.example.grpc.User.LoginRequest
                .newBuilder()
                .setUsername("admin")
                .setPassword("admin")
                .build();
        org.example.grpc.User.ApiResponse response = stub.login(loginRequest);
        System.out.printf(response.getResponseMessage());
    }
}
