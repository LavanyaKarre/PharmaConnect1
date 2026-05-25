package com.cts.mfrp.petz.models.testdata;

import com.cts.mfrp.petz.utils.XmlDataLoader;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Wrapper for the root {@code <registerCases>} element in
 * {@code src/test/resources/testdata/auth-register.xml}. Parsed once, cached.
 */
@JacksonXmlRootElement(localName = "registerCases")
public class RegisterCases {

    private static final String RESOURCE = "testdata/auth-register.xml";
    private static volatile RegisterCases cached;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "case")
    private List<RegisterCase> cases;

    public List<RegisterCase> getCases()            { return cases; }
    public void setCases(List<RegisterCase> value)  { this.cases = value; }

    public static RegisterCase byId(String id) {
        if (cached == null) {
            synchronized (RegisterCases.class) {
                if (cached == null) {
                    cached = XmlDataLoader.load(RESOURCE, RegisterCases.class);
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
