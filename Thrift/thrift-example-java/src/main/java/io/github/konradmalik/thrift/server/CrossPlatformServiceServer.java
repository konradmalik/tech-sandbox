package io.github.konradmalik.thrift.server;

import io.github.konradmalik.thrift.impl.CrossPlatformService;
import io.github.konradmalik.thrift.impl.CrossPlatformServiceImpl;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

public class CrossPlatformServiceServer {

    private TServer server;
    private final int port = 9090;

    public void start() throws TTransportException {
        TServerTransport serverTransport = new TServerSocket(port);
        server = new TSimpleServer(new TSimpleServer.Args(serverTransport)
                .processor(new CrossPlatformService.Processor<>(new CrossPlatformServiceImpl())));

        System.out.print("Starting the server on port " + port + "... ");

        server.serve();

        System.out.println("done.");
    }

    public void stop() {
        if (server != null && server.isServing()) {
            System.out.print("Stopping the server... ");

            server.stop();

            System.out.println("done.");
        }
    }

    public static void main(String[] args) throws TTransportException {

        CrossPlatformServiceServer crossPlatformServiceServer = new CrossPlatformServiceServer();
        crossPlatformServiceServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown Hook is running !");
            System.out.println("Application Terminating ...");
            crossPlatformServiceServer.stop();
        }));
    }

}
