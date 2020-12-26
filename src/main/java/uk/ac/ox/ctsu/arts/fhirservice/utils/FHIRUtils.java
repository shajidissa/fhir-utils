package uk.ac.ox.ctsu.arts.fhirservice.utils;

import org.hl7.fhir.r4.model.*;
import uk.ac.ox.ctsu.arts.fhirservice.helpers.FHIRClientHelper;
import uk.ac.ox.ctsu.arts.fhirservice.model.*;

public class FHIRUtils {
    private String FHIR_SERVER = "https://mts-fhir-server.azurewebsites.net";
    FHIRClientHelper fhirClientHelper;

    FHIRUtils() throws Exception {
        fhirClientHelper = new FHIRClientHelper(FHIR_SERVER);
    }

    public String createPatient(MTSPatient mtsPatient) {
        Patient patient = new Patient();
        patient.addName().setFamily(mtsPatient.getSurname()).addGiven(mtsPatient.getFirstname());
        if (mtsPatient.getMothersMaidenName() != null) {
            Extension ext = new Extension();
            ext.setUrl("http://hl7.org/fhir/StructureDefinition/patient-mothersMaidenName");
            ext.setValue(new StringType(mtsPatient.getMothersMaidenName()));
            patient.addExtension(ext);
        }
        return fhirClientHelper.createResource(patient).getId().toString();
    }

    public String getPatient(String firstname) {
        return fhirClientHelper.searchResource("Patient",Patient.NAME.matches().value(firstname));
    }

    public String createLocation(MTSLocation mtsLocation) {
        Location location = new Location();
        location.setName(mtsLocation.getName());
        location.setDescription(mtsLocation.getDescription());
        return fhirClientHelper.createResource(location).getId().toString();
    }

    public String getLocation(String name) {
        return fhirClientHelper.searchResource("Location",Location.NAME.matches().value(name));
    }

   public String createOrganization(MTSOrganisation mtsOrganisation) {
        Organization organization = new Organization();
        organization.setName(mtsOrganisation.getName());
        if (mtsOrganisation.getParentOrganisationId() != null) {
            Organization o2 = fhirClientHelper.findOrganizationByID(mtsOrganisation.getParentOrganisationId());
            organization.setPartOf(new Reference(o2));
        }
        return fhirClientHelper.createResource(organization).getId().toString();
    }

    public String getOrganization(String name) {
        return fhirClientHelper.searchResource("Organization",Location.NAME.matches().value(name));
    }

    public String createResearchStudy(MTSResearchStudy mtsResearchStudy) {
        ResearchStudy researchStudy = new ResearchStudy();
        researchStudy.setStatus(ResearchStudy.ResearchStudyStatus.ACTIVE);
        researchStudy.setTitle(mtsResearchStudy.getTitle());
        return fhirClientHelper.createResource(researchStudy).getId().toString();
    }

    public String getResearchStudy(String title) {
        return fhirClientHelper.searchResource("ResearchStudy",ResearchStudy.TITLE.matches().value(title));
    }

}
