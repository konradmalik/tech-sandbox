package io.github.konradmalik.soap.uc.shared;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
public interface UC {

    @WebMethod
    double c2f(double degrees);

    @WebMethod
    double cm2in(double cm);

    @WebMethod
    double f2c(double degrees);

    @WebMethod
    double in2cm(double in);

}
