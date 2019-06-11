package spark.to.s3.processor.census.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonAppend;

import java.io.Serializable;

@JsonPropertyOrder({
        "CensusTract", "State", "County", "TotalPop", "Men", "Women", "Hispanic", "White", "Black", "Native", "Asian",
        "Pacific", "Citizen", "Income", "IncomeErr", "IncomePerCap", "IncomePerCapErr", "Poverty", "ChildPoverty",
        "Professional", "Service", "Office", "Construction", "Production", "Drive", "Carpool", "Transit", "Walk", "OtherTransp",
        "WorkAtHome", "MeanCommute", "Employed", "PrivateWork", "PublicWork", "SelfEmployed", "FamilyWork", "Unemployment"
})
@JsonAppend(attrs = {@JsonAppend.Attr(value = "OtherEthnicity")})
@JsonIgnoreProperties(ignoreUnknown = true)
public class CensusRecord implements Serializable {
    @JsonProperty("CensusTract")
    private String CensusTract;
    @JsonProperty("State")
    private String State;
    @JsonProperty("County")
    private String County;
    @JsonProperty("TotalPop")
    private Double TotalPop;
    @JsonProperty("Men")
    private String Men;
    @JsonProperty("Women")
    private String Women;
    @JsonProperty("Hispanic")
    private Double Hispanic;
    @JsonProperty("White")
    private Double White;
    @JsonProperty("Black")
    private Double Black;
    @JsonProperty("Native")
    private Double Native;
    @JsonProperty("Asian")
    private Double Asian;
    @JsonProperty("Pacific")
    private Double Pacific;
    @JsonProperty("Citizen")
    private String Citizen;
    @JsonProperty("Income")
    private String Income;
    @JsonProperty("IncomeErr")
    private String IncomeErr;
    @JsonProperty("IncomePerCap")
    private String IncomePerCap;
    @JsonProperty("IncomePerCapErr")
    private String IncomePerCapErr;
    @JsonProperty("Poverty")
    private String Poverty;
    @JsonProperty("ChildPoverty")
    private String ChildPoverty;
    @JsonProperty("Professional")
    private String Professional;
    @JsonProperty("Service")
    private String Service;
    @JsonProperty("Office")
    private String Office;
    @JsonProperty("Construction")
    private String Construction;
    @JsonProperty("Production")
    private String Production;
    @JsonProperty("Drive")
    private String Drive;
    @JsonProperty("Carpool")
    private String Carpool;
    @JsonProperty("Transit")
    private String Transit;
    @JsonProperty("Walk")
    private String Walk;
    @JsonProperty("OtherTransp")
    private String OtherTransp;
    @JsonProperty("WorkAtHome")
    private String WorkAtHome;
    @JsonProperty("MeanCommute")
    private String MeanCommute;
    @JsonProperty("Employed")
    private String Employed;
    @JsonProperty("PrivateWork")
    private String PrivateWork;
    @JsonProperty("PublicWork")
    private String PublicWork;
    @JsonProperty("SelfEmployed")
    private String SelfEmployed;
    @JsonProperty("FamilyWork")
    private String FamilyWork;
    @JsonProperty("Unemployment")
    private String Unemployment;

    public String getCensusTract() {
        return CensusTract;
    }

    public void setCensusTract(String censusTract) {
        CensusTract = censusTract;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public String getCounty() {
        return County;
    }

    public void setCounty(String county) {
        County = county;
    }

    public Double getTotalPop() {
        return TotalPop;
    }

    public void setTotalPop(Double totalPop) {
        TotalPop = totalPop;
    }

    public String getMen() {
        return Men;
    }

    public void setMen(String men) {
        Men = men;
    }

    public String getWomen() {
        return Women;
    }

    public void setWomen(String women) {
        Women = women;
    }

    public Double getHispanic() {
        return Hispanic;
    }

    public void setHispanic(Double hispanic) {
        Hispanic = hispanic;
    }

    public Double getWhite() {
        return White;
    }

    public void setWhite(Double white) {
        White = white;
    }

    public Double getBlack() {
        return Black;
    }

    public void setBlack(Double black) {
        Black = black;
    }

    public Double getNative() {
        return Native;
    }

    public void setNative(Double aNative) {
        Native = aNative;
    }

    public Double getAsian() {
        return Asian;
    }

    public void setAsian(Double asian) {
        Asian = asian;
    }

    public Double getPacific() {
        return Pacific;
    }

    public void setPacific(Double pacific) {
        Pacific = pacific;
    }

    public String getCitizen() {
        return Citizen;
    }

    public void setCitizen(String citizen) {
        Citizen = citizen;
    }

    public String getIncome() {
        return Income;
    }

    public void setIncome(String income) {
        Income = income;
    }

    public String getIncomeErr() {
        return IncomeErr;
    }

    public void setIncomeErr(String incomeErr) {
        IncomeErr = incomeErr;
    }

    public String getIncomePerCap() {
        return IncomePerCap;
    }

    public void setIncomePerCap(String incomePerCap) {
        IncomePerCap = incomePerCap;
    }

    public String getIncomePerCapErr() {
        return IncomePerCapErr;
    }

    public void setIncomePerCapErr(String incomePerCapErr) {
        IncomePerCapErr = incomePerCapErr;
    }

    public String getPoverty() {
        return Poverty;
    }

    public void setPoverty(String poverty) {
        Poverty = poverty;
    }

    public String getChildPoverty() {
        return ChildPoverty;
    }

    public void setChildPoverty(String childPoverty) {
        ChildPoverty = childPoverty;
    }

    public String getProfessional() {
        return Professional;
    }

    public void setProfessional(String professional) {
        Professional = professional;
    }

    public String getService() {
        return Service;
    }

    public void setService(String service) {
        Service = service;
    }

    public String getOffice() {
        return Office;
    }

    public void setOffice(String office) {
        Office = office;
    }

    public String getConstruction() {
        return Construction;
    }

    public void setConstruction(String construction) {
        Construction = construction;
    }

    public String getProduction() {
        return Production;
    }

    public void setProduction(String production) {
        Production = production;
    }

    public String getDrive() {
        return Drive;
    }

    public void setDrive(String drive) {
        Drive = drive;
    }

    public String getCarpool() {
        return Carpool;
    }

    public void setCarpool(String carpool) {
        Carpool = carpool;
    }

    public String getTransit() {
        return Transit;
    }

    public void setTransit(String transit) {
        Transit = transit;
    }

    public String getWalk() {
        return Walk;
    }

    public void setWalk(String walk) {
        Walk = walk;
    }

    public String getOtherTransp() {
        return OtherTransp;
    }

    public void setOtherTransp(String otherTransp) {
        OtherTransp = otherTransp;
    }

    public String getWorkAtHome() {
        return WorkAtHome;
    }

    public void setWorkAtHome(String workAtHome) {
        WorkAtHome = workAtHome;
    }

    public String getMeanCommute() {
        return MeanCommute;
    }

    public void setMeanCommute(String meanCommute) {
        MeanCommute = meanCommute;
    }

    public String getEmployed() {
        return Employed;
    }

    public void setEmployed(String employed) {
        Employed = employed;
    }

    public String getPrivateWork() {
        return PrivateWork;
    }

    public void setPrivateWork(String privateWork) {
        PrivateWork = privateWork;
    }

    public String getPublicWork() {
        return PublicWork;
    }

    public void setPublicWork(String publicWork) {
        PublicWork = publicWork;
    }

    public String getSelfEmployed() {
        return SelfEmployed;
    }

    public void setSelfEmployed(String selfEmployed) {
        SelfEmployed = selfEmployed;
    }

    public String getFamilyWork() {
        return FamilyWork;
    }

    public void setFamilyWork(String familyWork) {
        FamilyWork = familyWork;
    }

    public String getUnemployment() {
        return Unemployment;
    }

    public void setUnemployment(String unemployment) {
        Unemployment = unemployment;
    }

    @Override
    public String toString() {
        return "CensusRecord{" +
                "CensusTract='" + CensusTract + '\'' +
                ", State='" + State + '\'' +
                ", County='" + County + '\'' +
                ", TotalPop='" + TotalPop + '\'' +
                ", Men='" + Men + '\'' +
                ", Women='" + Women + '\'' +
                ", Hispanic='" + Hispanic + '\'' +
                ", White='" + White + '\'' +
                ", Black='" + Black + '\'' +
                ", Native='" + Native + '\'' +
                ", Asian='" + Asian + '\'' +
                ", Pacific='" + Pacific + '\'' +
                ", Citizen='" + Citizen + '\'' +
                ", Income='" + Income + '\'' +
                ", IncomeErr='" + IncomeErr + '\'' +
                ", IncomePerCap='" + IncomePerCap + '\'' +
                ", IncomePerCapErr='" + IncomePerCapErr + '\'' +
                ", Poverty='" + Poverty + '\'' +
                ", ChildPoverty='" + ChildPoverty + '\'' +
                ", Professional='" + Professional + '\'' +
                ", Service='" + Service + '\'' +
                ", Office='" + Office + '\'' +
                ", Construction='" + Construction + '\'' +
                ", Production='" + Production + '\'' +
                ", Drive='" + Drive + '\'' +
                ", Carpool='" + Carpool + '\'' +
                ", Transit='" + Transit + '\'' +
                ", Walk='" + Walk + '\'' +
                ", OtherTransp='" + OtherTransp + '\'' +
                ", WorkAtHome='" + WorkAtHome + '\'' +
                ", MeanCommute='" + MeanCommute + '\'' +
                ", Employed='" + Employed + '\'' +
                ", PrivateWork='" + PrivateWork + '\'' +
                ", PublicWork='" + PublicWork + '\'' +
                ", SelfEmployed='" + SelfEmployed + '\'' +
                ", FamilyWork='" + FamilyWork + '\'' +
                ", Unemployment='" + Unemployment + '\'' +
                '}';
    }
}
