package com.cts.mfrp.petz.models.testdata;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

/**
 * One row in {@code src/test/resources/testdata/auth-register.xml}, consumed by
 * TC102_AuthRegisterTest. Carries per-case input hints and expected response
 * values. The literal seed email behind {@link #useSeededEmail} is not stored
 * here — the test method resolves it from
 * {@link com.cts.mfrp.petz.constants.AppConstants} at runtime.
 */
public class RegisterCase {

    private String id;
    private String description;

    // ── Payload assembly ──
    private String payloadMode;        // EMPTY | GENERATED (default GENERATED)
    private String emailSuffix;        // GENERATED mode — unique-email seed
    private String emailOverride;      // post-generation literal override
    private String passwordOverride;   // post-generation literal override
    private String useSeededEmail;     // role key resolved against AppConstants

    // ── Expectations ──
    private Integer expectedStatus;
    private Integer expectedStatusMin;
    private Integer expectedStatusMax;
    private Boolean expectedSuccess;
    private String  expectedMessage;

    @JacksonXmlElementWrapper(localName = "expectedFields")
    @JacksonXmlProperty(localName = "field")
    private List<ExpectedField> expectedFields;

    private String  expectedFieldPresent;   // assert data.{name} non-null (no exact value)
    private String  expectedRole;
    private Boolean expectedIsApproved;
    private Boolean expectedTokenPresent;

    public String  getId()                              { return id; }
    public void    setId(String value)                  { this.id = value; }

    public String  getDescription()                     { return description; }
    public void    setDescription(String value)         { this.description = value; }

    public String  getPayloadMode()                     { return payloadMode; }
    public void    setPayloadMode(String value)         { this.payloadMode = value; }

    public String  getEmailSuffix()                     { return emailSuffix; }
    public void    setEmailSuffix(String value)         { this.emailSuffix = value; }

    public String  getEmailOverride()                   { return emailOverride; }
    public void    setEmailOverride(String value)       { this.emailOverride = value; }

    public String  getPasswordOverride()                { return passwordOverride; }
    public void    setPasswordOverride(String value)    { this.passwordOverride = value; }

    public String  getUseSeededEmail()                  { return useSeededEmail; }
    public void    setUseSeededEmail(String value)      { this.useSeededEmail = value; }

    public Integer getExpectedStatus()                  { return expectedStatus; }
    public void    setExpectedStatus(Integer value)     { this.expectedStatus = value; }

    public Integer getExpectedStatusMin()               { return expectedStatusMin; }
    public void    setExpectedStatusMin(Integer value)  { this.expectedStatusMin = value; }

    public Integer getExpectedStatusMax()               { return expectedStatusMax; }
    public void    setExpectedStatusMax(Integer value)  { this.expectedStatusMax = value; }

    public Boolean getExpectedSuccess()                 { return expectedSuccess; }
    public void    setExpectedSuccess(Boolean value)    { this.expectedSuccess = value; }

    public String  getExpectedMessage()                 { return expectedMessage; }
    public void    setExpectedMessage(String value)     { this.expectedMessage = value; }

    public List<ExpectedField> getExpectedFields()             { return expectedFields; }
    public void setExpectedFields(List<ExpectedField> value)   { this.expectedFields = value; }

    public String  getExpectedFieldPresent()            { return expectedFieldPresent; }
    public void    setExpectedFieldPresent(String value){ this.expectedFieldPresent = value; }

    public String  getExpectedRole()                    { return expectedRole; }
    public void    setExpectedRole(String value)        { this.expectedRole = value; }

    public Boolean getExpectedIsApproved()              { return expectedIsApproved; }
    public void    setExpectedIsApproved(Boolean value) { this.expectedIsApproved = value; }

    public Boolean getExpectedTokenPresent()            { return expectedTokenPresent; }
    public void    setExpectedTokenPresent(Boolean v)   { this.expectedTokenPresent = v; }
}
