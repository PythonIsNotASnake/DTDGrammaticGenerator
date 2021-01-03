package de.kejukedor.generator.fileAccess;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class XMLReader {

    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;

    public XMLReader() {
        this.factory = DocumentBuilderFactory.newInstance();
        try {
            this.builder = this.factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public Document read(){
        return this.generateDocument(this.choosePath());
    }

    private String choosePath() {
        String dataPath = null;
        JFileChooser chooser = new JFileChooser("src/de/kejukedor/generator");
        int rueckgabeWert = chooser.showOpenDialog(null);
        if (rueckgabeWert == JFileChooser.APPROVE_OPTION) {
            dataPath = chooser.getSelectedFile().getAbsolutePath();
            return dataPath;
        }
        return null;
    }

    private Document generateDocument(String path) {
        if(path!=null){
            Document doc = null;
            try {
                doc = this.builder.parse(new File(path));
                doc.getDocumentElement().normalize();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return doc;
        }else{
            return null;
        }
    }
}
