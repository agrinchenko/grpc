package org.example.service;

import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.Context;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.example.config.ConfigurationHelper;
import org.example.grpc.RadiationProviderGrpc;
import org.example.grpc.User;
import org.example.supplier.RadiationSupplier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;

public class RadiationService extends RadiationProviderGrpc.RadiationProviderImplBase{
    private RadiationSupplier _radiationSupplier;
    private BlockingQueue<Short> _cpmQueue;

    @Override
    public void serverSideStreamingCpmCounts(User.Empty request, StreamObserver<User.CpmCount> responseObserver) {
        ServerCallStreamObserver<User.CpmCount> serverCallStreamObserver =
                (ServerCallStreamObserver<User.CpmCount>) responseObserver;

        serverCallStreamObserver.setOnCancelHandler(() -> {
            System.out.println("Client disconnected.");
            stopDataEmit();
        });
        serverCallStreamObserver.setOnReadyHandler(() -> System.out.println("Client is ready."));

        startDataEmit();
        while (true) {
            wait(1000);

            if (Context.current().isCancelled() || serverCallStreamObserver.isCancelled()) {
                stopDataEmit();
                serverCallStreamObserver.onCompleted();
                break;
            }

            Short cpm = _cpmQueue.poll();
            if (cpm == null) {
                continue;
            }

            User.CpmCount radCount = User.CpmCount.newBuilder()
                    .setCount(cpm)
                    .build();
            responseObserver.onNext(radCount);
        }
        responseObserver.onCompleted();
    }

    private void stopDataEmit() {
        _radiationSupplier.stopDataEmit();
    }

    private void startDataEmit() {
        _radiationSupplier.startDataEmit();
    }

    private static void wait(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void initialize(String configPath) {
        _radiationSupplier = new RadiationSupplier();

        ConfigurationHelper.applyConfigurationByPath(new ArrayList<>(Collections.singletonList(_radiationSupplier)), configPath);
        boolean initOK = _radiationSupplier.initialize();
        if (!initOK) {
            System.out.println("RadiationService failed to initialize");
            System.exit(1);
        }
        _cpmQueue = _radiationSupplier.getQueue();
    }


    private void startDataAcquire() {
        _radiationSupplier.registerPortDataListener();
        Thread thread = new Thread(() ->_radiationSupplier.startDataAcquire());
        thread.start();
        wait(3000); //wait for data acquisition to start
    }


    public void connectToUsbDevice() {
        startDataAcquire();
    }
}
