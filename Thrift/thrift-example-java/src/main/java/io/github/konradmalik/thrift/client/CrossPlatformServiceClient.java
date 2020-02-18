package io.github.konradmalik.thrift.client;

import io.github.konradmalik.thrift.impl.CrossPlatformResource;
import io.github.konradmalik.thrift.impl.CrossPlatformService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class CrossPlatformServiceClient {

    public static void main(String[] args) throws TException {
        TTransport transport = new TSocket("localhost", 9090);
        transport.open();

        TProtocol protocol = new TBinaryProtocol(transport);
        CrossPlatformService.Client client = new CrossPlatformService.Client(protocol);

        boolean result = client.ping();
        System.out.println(result);

        client.save(new CrossPlatformResource(0, "Jurek").setSalutation("mister"));
        System.out.println(client.get(0).salutation);

        transport.close();
    }

}
