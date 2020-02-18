package io.github.konradmalik.soap.RomanConverter;

import javax.swing.*;
import javax.swing.border.Border;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Iterator;

class RomanNumeralsJFrame extends JFrame {
    private JTextField txtResult;

    RomanNumeralsJFrame() {
        super("RomanNumerals");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Create a gradient panel in which to present the GUI.
        GPanel pnl = new GPanel();
        pnl.setLayout(new BorderLayout());
        // Build input panel.
        JPanel pnlInput = new JPanel();
        Border inner = BorderFactory.createEtchedBorder();
        Border outer = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        pnlInput.setBorder(BorderFactory.createCompoundBorder(outer, inner));
        pnlInput.setOpaque(false);
        pnlInput.add(new JLabel("Enter Roman numerals or integer:"));
        final JTextField txtInput = new JTextField(15);
        pnlInput.add(txtInput);
        pnl.add(pnlInput, BorderLayout.NORTH);
        // Build buttons panel.
        JPanel pnlButtons = new JPanel();
        inner = BorderFactory.createEtchedBorder();
        outer = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        pnlButtons.setBorder(BorderFactory.createCompoundBorder(outer, inner));
        pnlButtons.setOpaque(false);
        JButton btnToRoman = new JButton("To Roman");
        ActionListener alToRoman;
        alToRoman = (ae) ->
        {
            try {
                String roman = toRoman(txtInput.getText());
                txtResult.setText(roman);
            } catch (SOAPException se) {
                JOptionPane.showMessageDialog(RomanNumeralsJFrame.this,
                        se.getMessage());
            }
        };
        btnToRoman.addActionListener(alToRoman);
        pnlButtons.add(btnToRoman);
        JButton btnToInteger = new JButton("To Integer");
        ActionListener alToInteger;
        alToInteger = (ae) ->
        {
            try {
                String integer = toInteger(txtInput.getText());
                txtResult.setText(integer);
            } catch (SOAPException se) {
                JOptionPane.showMessageDialog(RomanNumeralsJFrame.this,
                        se.getMessage());
            }
        };
        btnToInteger.addActionListener(alToInteger);
        pnlButtons.add(btnToInteger);
        pnl.add(pnlButtons, BorderLayout.CENTER);
        // Build result panel.
        JPanel pnlResult = new JPanel();
        inner = BorderFactory.createEtchedBorder();
        outer = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        pnlResult.setBorder(BorderFactory.createCompoundBorder(outer, inner));
        pnlResult.setOpaque(false);
        pnlResult.add(new JLabel("Result:"));
        txtResult = new JTextField(35);
        pnlResult.add(txtResult);
        pnl.add(pnlResult, BorderLayout.SOUTH);
        setContentPane(pnl);
        pack();
        setResizable(false);
        setLocationRelativeTo(null); // center on the screen
        setVisible(true);
    }

    private String toInteger(String input) throws SOAPException {
        return buildSOAPMessageForMethod(input, "RomanToInt", "RomanToIntResponse");
    }

    private String toRoman(String input) throws SOAPException {
        return buildSOAPMessageForMethod(input, "IntToRoman", "IntToRomanResponse");
    }

    private String buildSOAPMessageForMethod(String input, String requestLocalPart, String responseLocalPart) throws SOAPException {
        String requestNamespaceURI = "http://javajeff.ca/";
        String requestPrefix = "tns";
        String responseNamespaceURI = "urn:Roman-IRoman";
        String responsePrefix = "NS1";

        // Build a request message. The first step is to create an empty message
        // via a message factory. The default SOAP 1.1 message factory is used.
        MessageFactory mfactory = MessageFactory.newInstance();
        SOAPMessage request = mfactory.createMessage();
        // The request SOAPMessage object contains a SOAPPart object, which
        // contains a SOAPEnvelope object, which contains an empty SOAPHeader
        // object followed by an empty SOAPBody object.
        // Detach the header since a header is not required. This step is
        // optional.
        SOAPHeader header = request.getSOAPHeader();
        header.detachNode();
        // Access the body so that content can be added.
        SOAPBody body = request.getSOAPBody();
        // Add the RomanToInt operation body element to the body.
        QName bodyName = new QName(requestNamespaceURI, requestLocalPart, requestPrefix);
        SOAPBodyElement bodyElement = body.addBodyElement(bodyName);
        // Add the proper child element to the body element.
        String childPart;
        if(requestLocalPart.equalsIgnoreCase("RomanToInt"))
            childPart = "Rom";
        else
            childPart = "Int";
        QName name = new QName(childPart);
        SOAPElement element = bodyElement.addChildElement(name);
        element.addTextNode(input).setAttribute("xsi:type", "xs:string");
        // Add appropriate namespaces and an encoding style to the envelope.
        SOAPEnvelope env = request.getSOAPPart().getEnvelope();
        env.addNamespaceDeclaration("env",
                "http://schemas.xmlsoap.org/soap/envelop/");
        env.addNamespaceDeclaration("enc",
                "http://schemas.xmlsoap.org/soap/encoding/");
        env.setEncodingStyle(SOAPConstants.URI_NS_SOAP_ENCODING);
        env.addNamespaceDeclaration("xs", "http://www.w3.org/2001/XMLSchema");
        env.addNamespaceDeclaration("xsi",
                "http://www.w3.org/2001/XMLSchema-instance");
        // Output the request just built to standard output, to see what the
        // SOAP message looks like (which is useful for debugging).
        System.out.println("\nSoap request:\n");
        try {
            request.writeTo(System.out);
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(RomanNumeralsJFrame.this,
                    ioe.getMessage());
        }
        System.out.println();
        // Prepare to send message by obtaining a connection factory and creating
        // a connection.
        SOAPConnectionFactory factory = SOAPConnectionFactory.newInstance();
        SOAPConnection con = factory.createConnection();
        // Identify the message's target.
        String endpoint = "http://www.javajeff.ca/php/rncws.php";
        // Call the Web service at the target using the request message. Capture
        // the response message and send it to standard output.
        SOAPMessage response = con.call(request, endpoint);
        System.out.println("\nSoap response:\n");
        try {
            response.writeTo(System.out);
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(RomanNumeralsJFrame.this,
                    ioe.getMessage());
        }
        // Close the connection to release resources.
        con.close();
        // Return a response consisting of the reason for a SOAP Fault or the
        // value of the RomanToIntResponse body element's return child element.
        if (response.getSOAPBody().hasFault())
            return response.getSOAPBody().getFault().getFaultString();
        else {
            body = response.getSOAPBody();
            bodyName = new QName(responseNamespaceURI, responseLocalPart, responsePrefix);
            Iterator iter = body.getChildElements(bodyName);
            bodyElement = (SOAPBodyElement) iter.next();
            iter = bodyElement.getChildElements(new QName("return"));
            return ((SOAPElement) iter.next()).getValue();
        }
    }
}

