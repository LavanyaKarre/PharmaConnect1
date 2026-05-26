package com.cts.mfrp.petz.models.testdata;

/**
 * One row in {@code src/test/resources/testdata/browse-animals.xml}, consumed
 * by BrowseAnimalsTest TC028 / TC029 / TC030. Carries only per-case inputs and
 * expectations; the Pet Owner credentials used to log in stay in
 * {@link com.cts.mfrp.petz.constants.AppConstants}.
 */
public class BrowseAnimalsCase {

    private String  id;
    private String  description;
    private String  searchMode;                // SPECIES | CITY
    private String  searchValue;
    private String  expectedUrlFragment;       // optional
    private Boolean expectedZeroCardsOrEmpty;  // optional — TC030
    private Boolean speciesLookupTolerant;     // optional — TC028 may bail if option missing

    public String  getId()                                { return id; }
    public void    setId(String value)                    { this.id = value; }

    public String  getDescription()                       { return description; }
    public void    setDescription(String value)           { this.description = value; }

    public String  getSearchMode()                        { return searchMode; }
    public void    setSearchMode(String value)            { this.searchMode = value; }

    public String  getSearchValue()                       { return searchValue; }
    public void    setSearchValue(String value)           { this.searchValue = value; }

    public String  getExpectedUrlFragment()               { return expectedUrlFragment; }
    public void    setExpectedUrlFragment(String value)   { this.expectedUrlFragment = value; }

    public Boolean getExpectedZeroCardsOrEmpty()          { return expectedZeroCardsOrEmpty; }
    public void    setExpectedZeroCardsOrEmpty(Boolean v) { this.expectedZeroCardsOrEmpty = v; }

    public Boolean getSpeciesLookupTolerant()             { return speciesLookupTolerant; }
    public void    setSpeciesLookupTolerant(Boolean v)    { this.speciesLookupTolerant = v; }
}
