package com.cts.mfrp.petz.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;

/**
 * Reads .xml test-data files into List&lt;Map&lt;String,String&gt;&gt; — matches Excel's shape
 * so step defs use one interface.
 *
 * Expected XML layout:
 *   &lt;data&gt;
 *     &lt;section name="pets"&gt;
 *       &lt;row caseId="..." field="..." value="..." expected="..."/&gt;
 *     &lt;/section&gt;
 *     &lt;section name="rescues"&gt;...&lt;/section&gt;
 *   &lt;/data&gt;
 */
public class XmlDataProvider {

    private static final Logger logger = LoggerFactory.getLogger(XmlDataProvider.class);

    /**
     * Returns every row inside the named section as a Map of attribute → value.
     */
    public static List<Map<String, String>> readSection(String classpathPath, String sectionName) {
        List<Map<String, String>> rows = new ArrayList<>();

        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathPath)) {
            if (is == null) {
                throw new RuntimeException("XML file not found on classpath: " + classpathPath);
            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            doc.getDocumentElement().normalize();

            NodeList sections = doc.getElementsByTagName("section");
            for (int i = 0; i < sections.getLength(); i++) {
                Element section = (Element) sections.item(i);
                if (!sectionName.equals(section.getAttribute("name"))) continue;

                NodeList rowNodes = section.getElementsByTagName("row");
                for (int j = 0; j < rowNodes.getLength(); j++) {
                    Element rowEl = (Element) rowNodes.item(j);
                    Map<String, String> attrs = new LinkedHashMap<>();
                    NamedNodeMap nm = rowEl.getAttributes();
                    for (int k = 0; k < nm.getLength(); k++) {
                        Node attr = nm.item(k);
                        attrs.put(attr.getNodeName(), attr.getNodeValue());
                    }
                    rows.add(attrs);
                }
                break; // first matching section wins
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read XML section '" + sectionName + "' from " + classpathPath, e);
        }

        logger.info("Read {} rows from {}::{}", rows.size(), classpathPath, sectionName);
        return rows;
    }

    /**
     * Find one row by its caseId attribute inside a section. Returns null if not found.
     */
    public static Map<String, String> findByCaseId(String classpathPath, String sectionName, String caseId) {
        for (Map<String, String> row : readSection(classpathPath, sectionName)) {
            if (caseId.equals(row.get("caseId"))) return row;
        }
        return null;
    }

    private XmlDataProvider() {}
}
