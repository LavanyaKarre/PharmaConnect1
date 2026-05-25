package com.cts.mfrp.petz.models.testdata;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

/**
 * One row in {@code src/test/resources/testdata/auth-login.xml}, consumed by
 * TC101_AuthLoginTest. Carries only per-case input/expectations; environment
 * config (API base URL) is still owned by AppConstants.
 */
public class LoginCase {

    private String  id;
    private String  description;

    private String  rawBody;            // present → use AuthClient.loginRaw
    private String  email;              // present → use AuthClient.login(email, password)
    private String  password;
    private String  randomEmailPrefix;  // present → generate {prefix}_{millis}@example.com

    private Integer expectedStatus;
    private Boolean expectedSuccess;
    private String  expectedMessage;

    @JacksonXmlElementWrapper(localName = "expectedFields")
    @JacksonXmlProperty(localName = "field")
    private List<ExpectedField> expectedFields;

    public String  getId()                              { return id; }
    public void    setId(String value)                  { this.id = value; }

    public String  getDescription()                     { return description; }
    public void    setDescription(String value)         { this.description = value; }

    public String  getRawBody()                         { return rawBody; }
    public void    setRawBody(String value)             { this.rawBody = value; }

    public String  getEmail()                           { return email; }
    public void    setEmail(String value)               { this.email = value; }

    public String  getPassword()                        { return password; }
    public void    setPassword(String value)            { this.password = value; }

    public String  getRandomEmailPrefix()               { return randomEmailPrefix; }
    public void    setRandomEmailPrefix(String value)   { this.randomEmailPrefix = value; }

    public Integer getExpectedStatus()                  { return expectedStatus; }
    public void    setExpectedStatus(Integer value)     { this.expectedStatus = value; }

    public Boolean getExpectedSuccess()                 { return expectedSuccess; }
    public void    setExpectedSuccess(Boolean value)    { this.expectedSuccess = value; }

    public String  getExpectedMessage()                 { return expectedMessage; }
    public void    setExpectedMessage(String value)     { this.expectedMessage = value; }

    public List<ExpectedField> getExpectedFields()             { return expectedFields; }
    public void setExpectedFields(List<ExpectedField> value)   { this.expectedFields = value; }
}
