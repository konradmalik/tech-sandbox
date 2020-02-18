package io.github.konradmalik.soap.uc.client;

import io.github.konradmalik.soap.uc.client.wsimport.UCImplService;
import io.github.konradmalik.soap.uc.shared.UC;

public class UCWsimportClient {

    public static void main(String[] args) {
        UCImplService ucImplService = new UCImplService();
        UC uc = ucImplService.getUCImplPort();

        System.out.printf("DC to DF: 37 DC = %f DF%n", uc.c2f(37.0));
        System.out.printf("CM to IN: 10 CM = %f IN%n", uc.cm2in(10));
        System.out.printf("DF to DC: 212 DF = %f DC%n", uc.f2c(212.0));
        System.out.printf("IN to CM: 10 IN = %f CM%n", uc.in2cm(10));
    }

}


