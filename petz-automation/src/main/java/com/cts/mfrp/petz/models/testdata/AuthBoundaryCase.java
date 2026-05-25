package com.cts.mfrp.petz.models.testdata;

/**
 * One row in {@code src/test/resources/testdata/auth-boundaries.xml}, looked up by
 * id from inside the corresponding @Test method in TC1101_AuthBoundariesTest.
 */
public class AuthBoundaryCase {

    private String  id;
    private String  description;
    private String  token;            // optional — only TC1101_2 sets a literal token
    private String  endpoint;         // optional — only TC1101_3 overrides USERS_ME
    private Integer expectedStatus;
    private String  expectedMessage;  // optional — only TC1101_3 asserts a body message

    public String  getId()                          { return id; }
    public void    setId(String value)              { this.id = value; }

    public String  getDescription()                 { return description; }
    public void    setDescription(String value)     { this.description = value; }

    public String  getToken()                       { return token; }
    public void    setToken(String value)           { this.token = value; }

    public String  getEndpoint()                    { return endpoint; }
    public void    setEndpoint(String value)        { this.endpoint = value; }

    public Integer getExpectedStatus()              { return expectedStatus; }
    public void    setExpectedStatus(Integer value) { this.expectedStatus = value; }

    public String  getExpectedMessage()             { return expectedMessage; }
    public void    setExpectedMessage(String value) { this.expectedMessage = value; }
}
