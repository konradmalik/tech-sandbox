package io.github.konradmalik.rest.image;

import javax.xml.ws.Endpoint;
import java.net.MalformedURLException;
import java.net.URL;

public class ImagePublisherServer {
    public static void main(String[] args) throws MalformedURLException {
        String PROTOCOL = "http";
        String HOST = "localhost";
        int PORT = 9902;
        String ENDPOINT = "/image";

        URL url = new URL(PROTOCOL, HOST, PORT, ENDPOINT);

        System.out.println("Running REST web server on " + url);
        Endpoint.publish(url.toString(), new ImagePublisher());
    }
}
