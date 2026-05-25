package com.cts.mfrp.petz.models.testdata;

import com.cts.mfrp.petz.utils.XmlDataLoader;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Wrapper for the root {@code <authBoundaryCases>} element in
 * {@code src/test/resources/testdata/auth-boundaries.xml}.
 *
 * <p>Test methods fetch a single row with {@link #byId(String)}; the XML is
 * parsed once and cached.
 */
@JacksonXmlRootElement(localName = "authBoundaryCases")
public class AuthBoundaryCases {

    private static final String RESOURCE = "testdata/auth-boundaries.xml";
    private static volatile AuthBoundaryCases cached;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "case")
    private List<AuthBoundaryCase> cases;

    public List<AuthBoundaryCase> getCases()           { return cases; }
    public void setCases(List<AuthBoundaryCase> value) { this.cases = value; }

    public static AuthBoundaryCase byId(String id) {
        if (cached == null) {
            synchronized (AuthBoundaryCases.class) {
                if (cached == null) {
                    cached = XmlDataLoader.load(RESOURCE, AuthBoundaryCases.class);
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
