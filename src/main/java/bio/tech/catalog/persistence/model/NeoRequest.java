package bio.tech.catalog.persistence.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;


@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
@NodeEntity
public class NeoRequest {
    @GraphId
    private Long id;

    private int numberRequest;
    private String userInfo;
    private String titleProject;
    private String researchQuestion;
    private String researchBioRequests;
    private String researchDataRequests;
    private String detailBioRequests;
    private String detailDataRequests;
    private boolean phenoDataRequested;
    private String researchEthics;
    private String benefitsAfrica;
    private String africanCollaborators;
    private String feasibility;
    private String consentApprovals;
    private String consentBioRequests;
    private String consentDataRequests;
    private String acronyms;
    private String pathDocuments;
    private String status;

    // getters and setters
    public Long getId() { return id; }
    public int getNumberRequest() { return numberRequest; }
    public String getUserInfo() { return userInfo; }
    public String getTitleProject() { return titleProject; }
    public String getResearchQuestion() { return researchQuestion; }
    public String getResearchBioRequests() { return researchBioRequests; }
    public String getResearchDataRequests() { return researchDataRequests; }
    public String getDetailBioRequests() { return detailBioRequests; }
    public String getDetailDataRequests() { return detailDataRequests; }
    public boolean getPhenoDataRequested() { return phenoDataRequested; }
    public String getResearchEthics() { return researchEthics; }
    public String getBenefitsAfrica() { return benefitsAfrica; }
    public String getAfricanCollaborators() { return africanCollaborators; }
    public String getFeasibility() { return feasibility; }
    public String getConsentApprovals() { return consentApprovals; }
    public String getConsentBioRequests() { return consentBioRequests; }
    public String getConsentDataRequests() { return consentDataRequests; }
    public String getAcronyms() { return acronyms; }
    public String getPathDocuments() { return pathDocuments; }
    public String getStatus() { return status; }

    public void setNumberRequest(int numberRequest) { this.numberRequest = numberRequest; }
    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }
    public void setTitleProject(String titleProject) {
        this.titleProject = titleProject;
    }
    public void setResearchQuestion(String researchQuestion) {
        this.researchQuestion = researchQuestion;
    }
    public void setResearchBioRequests(String researchBioRequests) {
        this.researchBioRequests = researchBioRequests;
    }
    public void setResearchDataRequests(String researchDataRequests) {
        this.researchDataRequests = researchDataRequests;
    }
    public void setDetailBioRequests(String detailBioRequests) {
        this.detailBioRequests = detailBioRequests;
    }
    public void setDetailDataRequests(String detailDataRequests) {
        this.detailDataRequests = detailDataRequests;
    }
    public void setPhenoDataRequested(boolean phenoDataRequested) {
        this.phenoDataRequested = phenoDataRequested;
    }
    public void setResearchEthics(String researchEthics) {
        this.researchEthics = researchEthics;
    }
    public void setBenefitsAfrica(String benefitsAfrica) {
        this.benefitsAfrica = benefitsAfrica;
    }
    public void setAfricanCollaborators(String africanCollaborators) {
        this.africanCollaborators = africanCollaborators;
    }
    public void setFeasibility(String feasibility) {
        this.feasibility = feasibility;
    }
    public void setConsentApprovals(String consentApprovals) {
        this.consentApprovals = consentApprovals;
    }
    public void setConsentBioRequests(String consentBioRequests) {
        this.consentBioRequests = consentBioRequests;
    }
    public void setConsentDataRequests(String consentDataRequests) {
        this.consentDataRequests = consentDataRequests;
    }
    public void setAcronyms(String acronyms) { this.acronyms = acronyms; }
    public void setPathDocuments(String pathDocuments) { this.pathDocuments = pathDocuments; }
    public void setStatus(String status) { this.status = status; }
}
