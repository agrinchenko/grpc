package org.example.service;

import io.grpc.stub.StreamObserver;
import org.example.grpc.User;
import org.example.grpc.userGrpc;

public class UserService extends userGrpc.userImplBase {
    @Override
    public void login(User.LoginRequest request, StreamObserver<User.ApiResponse> responseObserver) {
        String username = request.getUsername();
        String password = request.getPassword();

        User.ApiResponse.Builder response = User.ApiResponse.newBuilder();

        if (username.equals(password)) {
            //success
            response.setResponseCode("1").setResponseMessage("Sucess!");

        } else {
            //fail
            response.setResponseCode("0").setResponseMessage("Fail!");
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void logout(User.Empty request, StreamObserver<User.ApiResponse> responseObserver) {

    }
}
