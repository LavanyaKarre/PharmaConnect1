package com.cts.mfrp.petz.models.testdata;

import com.cts.mfrp.petz.utils.XmlDataLoader;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Wrapper for the root {@code <roleAccessCases>} element in
 * {@code src/test/resources/testdata/role-access.xml}. Parsed once and cached.
 */
@JacksonXmlRootElement(localName = "roleAccessCases")
public class RoleAccessCases {

    private static final String RESOURCE = "testdata/role-access.xml";
    private static volatile RoleAccessCases cached;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "case")
    private List<RoleAccessCase> cases;

    public List<RoleAccessCase> getCases()            { return cases; }
    public void setCases(List<RoleAccessCase> value)  { this.cases = value; }

    public static RoleAccessCase byId(String id) {
        if (cached == null) {
            synchronized (RoleAccessCases.class) {
                if (cached == null) {
                    cached = XmlDataLoader.load(RESOURCE, RoleAccessCases.class);
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
