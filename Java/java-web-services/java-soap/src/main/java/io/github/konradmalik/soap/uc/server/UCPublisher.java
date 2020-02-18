package io.github.konradmalik.soap.uc.server;

import javax.xml.ws.Endpoint;
import java.net.MalformedURLException;
import java.net.URL;

public class UCPublisher {

    public static void main(String[] args) throws MalformedURLException {
        String PROTOCOL = "http";
        String HOST = "localhost";
        int PORT = 9901;
        String ENDPOINT = "/UC";

        URL url = new URL(PROTOCOL, HOST, PORT, ENDPOINT);

        System.out.println("Running web server on " + url);
        Endpoint.publish(url.toString(), new UCImpl());
    }

}
