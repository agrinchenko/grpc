package org.example.client;

import org.example.client.consumers.InfluxEmitter;
import org.example.client.consumers.StdoutPrinter;

public class ClientMain {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.printf("Config Path argument is missing %n");
            System.exit(1);
        }
        String configPath = args[0];
//        GrpcRadiationClient<InfluxEmitter> client =new GrpcRadiationClient<>(new InfluxEmitter());
        GrpcRadiationClient<StdoutPrinter> client =new GrpcRadiationClient<>(new StdoutPrinter());
        boolean configuredOK = client.configure(configPath);
        if (!configuredOK) {
            System.out.printf("Failed to configure Data Director%s %n", configPath);
            System.exit(1);
        }
        client.run();
    }
}
