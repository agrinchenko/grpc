package org.example.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.annotations.Item;
import org.example.annotations.Parameter;
import org.example.client.consumers.DataDirector;
import org.example.config.ConfigurationHelper;
import org.example.grpc.User;

import java.util.Arrays;
import java.util.Iterator;

@Item(name = "GrpcRadClient")
public class GrpcRadiationClient<T extends DataDirector> {
    private final T director;

    @Parameter(name = "serverHost", description = "URL of the gRPC server")
    public String _serverHost;

    @Parameter(name = "serverPort", description = "Port of the gRPC server")
    public int _serverPort;

    public GrpcRadiationClient(T director) {
        this.director = director;
    }

    public boolean configure(String configPath){
        ConfigurationHelper.applyConfigurationByPath(Arrays.asList(this, director), configPath);
        return director.initialize();
    }

    public void run() {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(_serverHost, _serverPort)
                .usePlaintext(true)
                .build();

        org.example.grpc.RadiationProviderGrpc.RadiationProviderBlockingStub stub = org.example.grpc.RadiationProviderGrpc.newBlockingStub(channel);
        org.example.grpc.User.Empty empty = org.example.grpc.User.Empty.newBuilder().build();
        Iterator<User.CpmCount> response = stub.serverSideStreamingCpmCounts(empty);
        while (response.hasNext()) {
                User.CpmCount cp = response.next();
                director.direct(cp.getCount());
        }
    }
}
