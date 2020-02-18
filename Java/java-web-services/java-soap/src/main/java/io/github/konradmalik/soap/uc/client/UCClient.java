package io.github.konradmalik.soap.uc.client;

import io.github.konradmalik.soap.uc.shared.UC;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;

public class UCClient {

    public static void main(String[] args) throws MalformedURLException {
        String PROTOCOL = "http";
        String HOST = "localhost";
        int PORT = 9901;
        String ENDPOINT = "/UC?wsdl";

        URL url = new URL(PROTOCOL, HOST, PORT, ENDPOINT);

        QName qname = new QName("http://server.uc.soap.konradmalik.github.io/", "UCImplService");
        Service service = Service.create(url, qname);

        qname = new QName("http://server.uc.soap.konradmalik.github.io/", "UCImplPort");
        UC uc = service.getPort(qname, UC.class);

        System.out.printf("DC to DF: 37 DC = %f DF%n", uc.c2f(37.0));
        System.out.printf("CM to IN: 10 CM = %f IN%n", uc.cm2in(10));
        System.out.printf("DF to DC: 212 DF = %f DC%n", uc.f2c(212.0));
        System.out.printf("IN to CM: 10 IN = %f CM%n", uc.in2cm(10));
    }

}


