package de.kejukedor.generator;

import de.kejukedor.generator.fileAccess.XMLWriter;

public class StartApplication {

    public static void main(String[] args) {
        GeneratorAlgo algo = new GeneratorAlgo();
        algo.searchInXML(algo.getXmlA(), algo.getDtdA());
        algo.searchInXML(algo.getXmlB(), algo.getDtdB());
        XMLWriter writer = new XMLWriter();
        writer.createFile(algo.compareDTDs(algo.getDtdA(), algo.getDtdB()));
    }
}
