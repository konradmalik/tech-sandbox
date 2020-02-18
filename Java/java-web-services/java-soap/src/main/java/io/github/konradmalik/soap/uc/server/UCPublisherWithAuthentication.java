package io.github.konradmalik.soap.uc.server;

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;

public class UCPublisherWithAuthentication {

    public static void main(String[] args) throws IOException {
        String PROTOCOL = "http";
        String HOST = "localhost";
        int PORT = 9901;
        String ENDPOINT = "/UC";

        URL url = new URL(PROTOCOL, HOST, PORT, ENDPOINT);

        HttpServer server = HttpServer.create(new InetSocketAddress(url.getPort()), 0);
        HttpContext context = server.createContext(url.getPath());
        BasicAuthenticator auth;
        auth = new BasicAuthenticator("myAuth") {
            @Override
            public boolean checkCredentials(String username, String password) {
                return username.equals("myuser") && password.equals("mypass");
            }
        };
        context.setAuthenticator(auth);
        Endpoint endpoint = Endpoint.create(new UCImpl());
        endpoint.publish(context);
        System.out.println("Running web server on " + url);
        server.start();
    }

}
