package io.github.konradmalik.soap.uc.client;

import io.github.konradmalik.soap.uc.shared.UC;

import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.List;

public class UCClientWithAuthentication {

    public static void main(String[] args) throws MalformedURLException {
        Authenticator auth;
        auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("myuser", new char[]{'m','y','p','a','s','s'});
            }
        };
        Authenticator.setDefault(auth);

        String PROTOCOL = "http";
        String HOST = "localhost";
        int PORT = 9901;
        String ENDPOINT = "/UC?wsdl";

        URL url = new URL(PROTOCOL, HOST, PORT, ENDPOINT);

        QName qname = new QName("http://server.uc.soap.konradmalik.github.io/", "UCImplService");
        Service service = Service.create(url, qname);

        qname = new QName("http://server.uc.soap.konradmalik.github.io/", "UCImplPort");
        UC uc = service.getPort(qname, UC.class);

        // handler start
        BindingProvider bp = (BindingProvider) uc;
        Binding binding = bp.getBinding();
        List<Handler> hc = binding.getHandlerChain();
        hc.add(new SOAPLoggingHandler());
        binding.setHandlerChain(hc);
        // handler end

        System.out.printf("DC to DF: 37 DC = %f DF%n", uc.c2f(37.0));
        System.out.printf("CM to IN: 10 CM = %f IN%n", uc.cm2in(10));
        System.out.printf("DF to DC: 212 DF = %f DC%n", uc.f2c(212.0));
        System.out.printf("IN to CM: 10 IN = %f CM%n", uc.in2cm(10));
    }

}


