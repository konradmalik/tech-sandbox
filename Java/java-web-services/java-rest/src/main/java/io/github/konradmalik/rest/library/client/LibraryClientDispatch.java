package io.github.konradmalik.rest.library.client;

import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class LibraryClientDispatch {
    private final static String LIBURI = "http://localhost:9902/library";

    public static void main(String[] args) throws Exception {
        String book1 = "<?xml version=\"1.0\"?>" +
                "<book isbn=\"0201548550\" pubyear=\"1992\">" +
                "  <title>" +
                "    Advanced C++" +
                "  </title>" +
                "  <author>" +
                "    James O. Coplien" +
                "  </author>" +
                "  <publisher>" +
                "    Addison Wesley" +
                "  </publisher>" +
                "</book>";
        doPost(book1);
        String book2 = "<?xml version=\"1.0\"?>" +
                "<book isbn=\"9781430210450\" pubyear=\"2008\">" +
                "  <title>" +
                "    Beginning Groovy and Grails" +
                "  </title>" +
                "  <author>" +
                "    Christopher M. Judd" +
                "  </author>" +
                "  <author>" +
                "    Joseph Faisal Nusairat" +
                "  </author>" +
                "  <author>" +
                "    James Shingler" +
                "  </author>" +
                "  <publisher>" +
                "    Apress" +
                "  </publisher>" +
                "</book>";
        doPost(book2);
        doGet(null);
        doGet("0201548550");
        doGet("9781430210450");
        String book1u = "<?xml version=\"1.0\"?>" +
                "<book isbn=\"0201548550\" pubyear=\"1992\">" +
                "  <title>" +
                "    Advanced C++" +
                "  </title>" +
                "  <author>" +
                "    James O. Coplien" +
                "  </author>" +
                "  <publisher>" +
                "    Addison Wesley" +
                "  </publisher>" +
                "</book>";
        doPut(book1u);
        doGet("0201548550");
        doGetAsDispatch("0201548550");
        doDelete("0201548550");
        doGet(null);
    }

    private static void doGetOrDeleteMethod(String isbn, String method) throws IOException {
        URL url = new URL(LIBURI + ((isbn != null) ? "?isbn=" + isbn : ""));
        HttpURLConnection httpurlc = (HttpURLConnection) url.openConnection();
        httpurlc.setRequestMethod(method.trim().toUpperCase());
        httpurlc.setDoInput(true);

        handleProperResponse(httpurlc);

        System.out.println();
    }

    private static void doGetAsDispatch(String isbn) throws Exception {
        Service service = Service.create(new QName(""));
        String endpoint = "http://localhost:9902/library";
        service.addPort(new QName(""), HTTPBinding.HTTP_BINDING, endpoint);
        Dispatch<Source> dispatch;
        dispatch = service.createDispatch(new QName(""), Source.class,
                Service.Mode.MESSAGE);
        Map<String, Object> reqContext = dispatch.getRequestContext();
        reqContext.put(MessageContext.HTTP_REQUEST_METHOD, "GET");
        if (isbn != null)
            reqContext.put(MessageContext.QUERY_STRING, "isbn=" + isbn);
        Source result;
        try {
            result = dispatch.invoke(null);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return;
        }
        try {
            DOMResult dom = new DOMResult();
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.transform(result, dom);
            XPathFactory xpf = XPathFactory.newInstance();
            XPath xp = xpf.newXPath();
            if (isbn == null) {
                NodeList isbns = (NodeList) xp.evaluate("/isbns/isbn/text()",
                        dom.getNode(),
                        XPathConstants.NODESET);
                for (int i = 0; i < isbns.getLength(); i++)
                    System.out.println(isbns.item(i).getNodeValue());
            } else {
                NodeList books = (NodeList) xp.evaluate("/book", dom.getNode(),
                        XPathConstants.NODESET);
                isbn = xp.evaluate("@isbn", books.item(0));
                String pubYear = xp.evaluate("@pubyear", books.item(0));
                String title = xp.evaluate("title", books.item(0)).trim();
                String publisher = xp.evaluate("publisher", books.item(0)).trim();
                NodeList authors = (NodeList) xp.evaluate("author", books.item(0),
                        XPathConstants.NODESET);
                System.out.println("Title: " + title);
                for (int i = 0; i < authors.getLength(); i++)
                    System.out.println("Author: " + authors.item(i).getFirstChild()
                            .getNodeValue().trim());
                System.out.println("ISBN: " + isbn);
                System.out.println("Publication Year: " + pubYear);
                System.out.println("Publisher: " + publisher);
            }
        } catch (TransformerException | XPathExpressionException e) {
            System.err.println(e);
        }
        System.out.println();
    }

    private static HttpURLConnection doPutOrPostMethod(String xml) throws IOException {
        URL url = new URL(LIBURI);
        HttpURLConnection httpurlc = (HttpURLConnection) url.openConnection();
        httpurlc.setRequestMethod("POST");
        httpurlc.setDoOutput(true);
        httpurlc.setDoInput(true);
        httpurlc.setRequestProperty("Content-Type", "text/xml");
        OutputStream os = httpurlc.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        osw.write(xml);
        osw.close();
        return httpurlc;
    }

    private static void handleProperResponse(HttpURLConnection httpurlc) throws IOException {
        InputStreamReader isr;
        isr = new InputStreamReader(httpurlc.getInputStream());
        BufferedReader br = new BufferedReader(isr);
        StringBuilder xml = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null)
            xml.append(line);
        System.out.println(xml);
    }

    private static void doDelete(String isbn) throws Exception {
        doGetOrDeleteMethod(isbn, "DELETE");
    }

    private static void doGet(String isbn) throws Exception {
        doGetOrDeleteMethod(isbn, "GET");
    }

    private static void doPost(String xml) throws Exception {
        HttpURLConnection httpurlc = doPutOrPostMethod(xml);
        if (httpurlc.getResponseCode() == 200) {
            handleProperResponse(httpurlc);
        } else
            System.err.println("cannot insert book: " + httpurlc.getResponseCode());
        System.out.println();
    }

    private static void doPut(String xml) throws Exception {
        HttpURLConnection httpurlc = doPutOrPostMethod(xml);
        if (httpurlc.getResponseCode() == 200) {
            handleProperResponse(httpurlc);
        } else
            System.err.println("cannot update book: " + httpurlc.getResponseCode());
        System.out.println();
    }
}