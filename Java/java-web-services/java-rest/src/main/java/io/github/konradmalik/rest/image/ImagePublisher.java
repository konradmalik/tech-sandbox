package io.github.konradmalik.rest.image;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MimetypesFileTypeMap;
import javax.annotation.Resource;
import javax.xml.ws.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.http.HTTPException;

@WebServiceProvider
@ServiceMode(value = javax.xml.ws.Service.Mode.MESSAGE)
@BindingType(value = HTTPBinding.HTTP_BINDING)
public final class ImagePublisher implements Provider<DataSource> {
    @Resource
    private WebServiceContext wsContext;

    @Override
    public DataSource invoke(DataSource request) {
        if (wsContext == null)
            throw new RuntimeException("dependency injection failed on wsContext");
        MessageContext msgContext = wsContext.getMessageContext();
        if ("GET".equals(msgContext.get(MessageContext.HTTP_REQUEST_METHOD))) {
            return doGet();
        }
        throw new HTTPException(405);
    }

    private DataSource doGet() {
        FileDataSource fds = new FileDataSource("balstone.jpg");
        MimetypesFileTypeMap mtftm = new MimetypesFileTypeMap();
        mtftm.addMimeTypes("image/jpeg jpg");
        fds.setFileTypeMap(mtftm);
        System.out.println(fds.getContentType());
        return fds;
    }


}