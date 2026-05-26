package com.cts.mfrp.petz.models.testdata;

import com.cts.mfrp.petz.utils.XmlDataLoader;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Wrapper for the root {@code <browseAnimalsCases>} element in
 * {@code src/test/resources/testdata/browse-animals.xml}. Parsed once, cached.
 */
@JacksonXmlRootElement(localName = "browseAnimalsCases")
public class BrowseAnimalsCases {

    private static final String RESOURCE = "testdata/browse-animals.xml";
    private static volatile BrowseAnimalsCases cached;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "case")
    private List<BrowseAnimalsCase> cases;

    public List<BrowseAnimalsCase> getCases()            { return cases; }
    public void setCases(List<BrowseAnimalsCase> value)  { this.cases = value; }

    public static BrowseAnimalsCase byId(String id) {
        if (cached == null) {
            synchronized (BrowseAnimalsCases.class) {
                if (cached == null) {
                    cached = XmlDataLoader.load(RESOURCE, BrowseAnimalsCases.class);
                }
            }
        }
        return cached.cases.stream()
                .filter(c -> id.equals(c.getId()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "No <case> with id=" + id + " in " + RESOURCE));
    }
}
