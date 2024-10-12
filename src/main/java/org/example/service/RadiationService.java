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
import java.util.Queue;
import java.util.Random;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

public class RadiationService extends RadiationProviderGrpc.RadiationProviderImplBase{
    private RadiationSupplier _radiationSupplier;
    private Queue<Short> _cpmQueue;

    @Override
    public void serverSideStreamingCpmCounts(User.Empty request, StreamObserver<User.CpmCount> responseObserver) {

                ServerCallStreamObserver<User.CpmCount> serverCallStreamObserver =
                        (ServerCallStreamObserver<User.CpmCount>) responseObserver;


                // Add a cancellation handler to detect client disconnects
                serverCallStreamObserver.setOnCancelHandler(() -> {
                    System.out.println("Client disconnected.");
                    stopDataEmit();
                });

                ((ServerCallStreamObserver<User.CpmCount>) responseObserver).setOnReadyHandler(() -> {
                    System.out.println("Client is ready.");
                });

                ((ServerCallStreamObserver<User.CpmCount>) responseObserver).setOnCancelHandler(() -> {
                    System.out.println("Client is cancelled.");
                });

//        serverCallStreamObserver.onError(new Throwable("MSG"));

                serverCallStreamObserver.setOnCancelHandler(() -> {
                    System.out.println("Client disconnected.");
                    stopDataEmit();
                });

                serverCallStreamObserver.setOnReadyHandler(() -> {
                    System.out.println("Client is ready.");
                });

        Context.CancellationListener cl = new Context.CancellationListener() {
            @Override
            public void cancelled(Context context) {
                System.out.println("KKKKKKKKKKKKKK");
            }
        };
                Context.current().addListener(cl, directExecutor());

                Random random = new Random();
                while (true) {
                    wait(1000);
                    if (Context.current().isCancelled()) {
                        System.out.println("CANCELLED");
                        serverCallStreamObserver.onCompleted();
                        break;
                    }

                    if (serverCallStreamObserver.isCancelled()) {
                        System.out.println("cancelled");
                        serverCallStreamObserver.onCompleted();
                        break;
                    }

                    int rand = random.nextInt();
                    User.CpmCount radCount = User.CpmCount.newBuilder()
                            .setCount(rand)
                            .build();
                    responseObserver.onNext(radCount);
                }

//        startDataEmit();
//        int breakCount = 0;
//        while (true) {
//
//            if (Context.current().isCancelled()) {
//                System.out.println("CANCELLED");
//                serverCallStreamObserver.onCompleted();
//                break;
//            }
//
//            if (serverCallStreamObserver.isCancelled()) {
//                System.out.println("cancelled");
//                serverCallStreamObserver.onCompleted();
//                break;
//            }
//
//            if (!_cpmQueue.isEmpty()) {
//                wait(5000);
//                Short cpm = _cpmQueue.poll();
//                System.out.printf("Queue size after poll is %d %n", _cpmQueue.size());
//                if (cpm != null && cpm > 0) {
//                    User.CpmCount radCount = User.CpmCount.newBuilder()
//                            .setCount(cpm)
//                            .build();
//                    responseObserver.onNext(radCount);
//                }
//            }
//            else {
//                System.out.println("Queue is empty, waiting 10 seconds...");
//                wait(15000);
//                System.out.println(" 10 seconds is over, incrementing break count");
//                breakCount++;
//                System.out.printf("Incrementing break count: %d %n", breakCount);
//            }
//        }
//                responseObserver.onCompleted();
//            }

//        startDataEmit();
//        int breakCount = 0;
//        while (true) {
//
//            if (Context.current().isCancelled()) {
//                System.out.println("CANCELLED");
//                serverCallStreamObserver.onCompleted();
//                break;
//            }
//
//            if (serverCallStreamObserver.isCancelled()) {
//                System.out.println("cancelled");
//                serverCallStreamObserver.onCompleted();
//                break;
//            }
//
//            if (!_cpmQueue.isEmpty()) {
//                wait(5000);
//                Short cpm = _cpmQueue.poll();
//                System.out.printf("Queue size after poll is %d %n", _cpmQueue.size());
//                if (cpm != null && cpm > 0) {
//                    User.CpmCount radCount = User.CpmCount.newBuilder()
//                            .setCount(cpm)
//                            .build();
//                    responseObserver.onNext(radCount);
//                }
//            }
//            else {
//                System.out.println("Queue is empty, waiting 10 seconds...");
//                wait(15000);
//                System.out.println(" 10 seconds is over, incrementing break count");
//                breakCount++;
//                System.out.printf("Incrementing break count: %d %n", breakCount);
//            }
//        }
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
        _cpmQueue = _radiationSupplier.getQueue();

        ConfigurationHelper.applyConfigurationByPath(new ArrayList<>(Collections.singletonList(_radiationSupplier)), configPath);
        boolean initOK = _radiationSupplier.initialize();
        if (!initOK) {
            System.out.println("RadiationService failed to initialize");
            System.exit(1);
        }
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
