package com.dna.challenge.data.processor.census;

/**
 * Defines constants concerning Census data for use by underlying classes
 */
public interface CensusConstants {
    // Column to append to Census Data
    String ETHNICITY_COL = "OtherEthnicity";

    // Columns that will be in the result Census data set
    String[] RESULT_COLUMNS = new String[]{
            "CensusTract", "State", "County", "Men", "Women", "Hispanic", "White", "Black", "Native", "Asian",
            "Pacific", ETHNICITY_COL, "Professional", "Service", "Office", "Construction", "Production",
            "Employed", "PrivateWork", "PublicWork", "SelfEmployed", "FamilyWork", "Unemployment"
    };
}
