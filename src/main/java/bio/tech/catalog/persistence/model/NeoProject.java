package bio.tech.catalog.persistence.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.hibernate.validator.constraints.NotEmpty;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@NodeEntity
@JsonIgnoreProperties(ignoreUnknown = true)
public class NeoProject {

    @GraphId
    private Long id;

    @NotEmpty
//    @Index(primary = true, unique = true)
    private String projectId;
    @NotEmpty
    private String projectTitle;
    @NotEmpty
    private String researchQuestion;
    private String researchBioRequests;
    private String researchDataRequests;
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

    @Relationship(type = "HAS_CART")
    private List<NeoCart> carts = new ArrayList<>();

    @Relationship(type = "HAS_USER", direction = "INCOMING")
    private Collection<User> user;

    public NeoProject() { super(); }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getResearchQuestion() {
        return researchQuestion;
    }

    public void setResearchQuestion(String researchQuestion) {
        this.researchQuestion = researchQuestion;
    }

    public String getResearchBioRequests() {
        return researchBioRequests;
    }

    public void setResearchBioRequests(String researchBioRequests) {
        this.researchBioRequests = researchBioRequests;
    }

    public String getResearchDataRequests() {
        return researchDataRequests;
    }

    public void setResearchDataRequests(String researchDataRequests) {
        this.researchDataRequests = researchDataRequests;
    }

    public boolean isPhenoDataRequested() {
        return phenoDataRequested;
    }

    public void setPhenoDataRequested(boolean phenoDataRequested) {
        this.phenoDataRequested = phenoDataRequested;
    }

    public String getResearchEthics() {
        return researchEthics;
    }

    public void setResearchEthics(String researchEthics) {
        this.researchEthics = researchEthics;
    }

    public String getBenefitsAfrica() {
        return benefitsAfrica;
    }

    public void setBenefitsAfrica(String benefitsAfrica) {
        this.benefitsAfrica = benefitsAfrica;
    }

    public String getAfricanCollaborators() {
        return africanCollaborators;
    }

    public void setAfricanCollaborators(String africanCollaborators) {
        this.africanCollaborators = africanCollaborators;
    }

    public String getFeasibility() {
        return feasibility;
    }

    public void setFeasibility(String feasibility) {
        this.feasibility = feasibility;
    }

    public String getConsentApprovals() {
        return consentApprovals;
    }

    public void setConsentApprovals(String consentApprovals) {
        this.consentApprovals = consentApprovals;
    }

    public String getConsentBioRequests() {
        return consentBioRequests;
    }

    public void setConsentBioRequests(String consentBioRequests) {
        this.consentBioRequests = consentBioRequests;
    }

    public String getConsentDataRequests() {
        return consentDataRequests;
    }

    public void setConsentDataRequests(String consentDataRequests) {
        this.consentDataRequests = consentDataRequests;
    }

    public String getAcronyms() {
        return acronyms;
    }

    public void setAcronyms(String acronyms) {
        this.acronyms = acronyms;
    }

    public String getPathDocuments() {
        return pathDocuments;
    }

    public void setPathDocuments(String pathDocuments) {
        this.pathDocuments = pathDocuments;
    }

    public List<NeoCart> getCarts() {
        return carts;
    }

    public void addCart(NeoCart cart) {
        this.carts.add(cart);
    }

    public Collection<User> getUser() {
        return user;
    }

    public void setUser(Collection<User> user) {
        this.user = user;
    }
}
