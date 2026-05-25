package com.cts.mfrp.petz.models.testdata;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

/**
 * Represents one {@code <field name="...">value</field>} entry inside a fixture's
 * {@code <expectedFields>} block. The name attribute is the JSON key under
 * {@code response.data}; the element body is the expected string value.
 */
public class ExpectedField {

    @JacksonXmlProperty(isAttribute = true, localName = "name")
    private String name;

    @JacksonXmlText
    private String value;

    public String getName()              { return name; }
    public void setName(String value)    { this.name = value; }

    public String getValue()             { return value; }
    public void setValue(String value)   { this.value = value; }
}
