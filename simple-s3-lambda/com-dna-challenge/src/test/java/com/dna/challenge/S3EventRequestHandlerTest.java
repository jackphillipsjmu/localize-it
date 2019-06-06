package com.dna.challenge;

import com.dna.challenge.data.processor.s3.CensusCSVDataProcessor;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class S3EventRequestHandlerTest {

    @Test
    public void test() {
        String initialString = "CensusTract,State,County,TotalPop,Men,Women,Hispanic,White,Black,Native,Asian,Pacific,Citizen,Income,IncomeErr,IncomePerCap,IncomePerCapErr,Poverty,ChildPoverty,Professional,Service,Office,Construction,Production,Drive,Carpool,Transit,Walk,OtherTransp,WorkAtHome,MeanCommute,Employed,PrivateWork,PublicWork,SelfEmployed,FamilyWork,Unemployment\n" +
                "1001020100,Alabama,Autauga,1948,940,1008,0.9,87.4,7.7,0.3,0.6,0.0,1503,61838.0,11900.0,25713.0,4548.0,8.1,8.4,34.7,17.0,21.3,11.9,15.2,90.2,4.8,0.0,0.5,2.3,2.1,25.0,943,77.1,18.3,4.6,0.0,5.4";
        InputStream inputStream = new ByteArrayInputStream(initialString.getBytes());

        CensusCSVDataProcessor censusCSVDataProcessor = new CensusCSVDataProcessor(inputStream);
        String result = censusCSVDataProcessor.process();

        System.out.println(result);

    }
}
