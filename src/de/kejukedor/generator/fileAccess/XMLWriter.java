package de.kejukedor.generator.fileAccess;

import de.kejukedor.generator.DTD;

import java.io.FileWriter;
import java.io.IOException;

public class XMLWriter {

    public XMLWriter() {

    }

    public void createFile(DTD dtd) {
        try {
            FileWriter myWriter = new FileWriter("src/de/kejukedor/generator/dtd.dtd");
            myWriter.write(dtd.toString());
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

}
