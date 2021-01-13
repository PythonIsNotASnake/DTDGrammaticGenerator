package de.generator;

import de.generator.fileAccess.XMLWriter;

public class StartApplication {

    public static void main(String[] args) {
        XMLToDTDAlgorithm xmlToDTDAlgorithm = new XMLToDTDAlgorithm();
        DTD dtdA = xmlToDTDAlgorithm.startAlgorithm();
        DTD dtdB = xmlToDTDAlgorithm.startAlgorithm();

        CombinedDTDAlgorithm combinedDTDAlgorithm = new CombinedDTDAlgorithm();
        XMLWriter writer = new XMLWriter();
        writer.createFile(combinedDTDAlgorithm.startAlgorithm(dtdA, dtdB));
    }
}
