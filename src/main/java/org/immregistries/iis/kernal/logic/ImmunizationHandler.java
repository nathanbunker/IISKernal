package org.immregistries.iis.kernal.logic;

import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Patient;
import org.immregistries.iis.kernal.model.*;

public class ImmunizationHandler {

    public static void patientReportedFromFhirImmunization(PatientReported patientReported, Immunization i) {
        patientReported.setReportedDate(i.getRecorded());
        patientReported.setUpdatedDate(i.getOccurrenceDateTimeType().getValue());
        patientReported.setPatientReportedAuthority(i.getIdentifierFirstRep().getValue());
        //patientReported.setPatientReportedType(patientReportedType);
    }

    public static void vaccinationReportedFromFhirImmunization(VaccinationReported vaccinationReported, Immunization i) {
        //vaccinationReported.setVaccinationReportedId(0);
        vaccinationReported.setVaccinationReportedExternalLink(i.getId());
        vaccinationReported.setReportedDate(i.getRecorded());
        vaccinationReported.setUpdatedDate(i.getOccurrenceDateTimeType().getValue());
        vaccinationReported.setLotnumber(i.getLotNumber());
        vaccinationReported.setAdministeredDate(i.getOccurrenceDateTimeType().getValue());
        vaccinationReported.setAdministeredAmount(i.getDoseQuantity().getValue().toString());
        vaccinationReported.setExpirationDate(i.getExpirationDate());
        switch (i.getStatus().toCode()){
            case "completed" : vaccinationReported.setCompletionStatus("CP");
            case "entered-in-error" : vaccinationReported.setCompletionStatus("entered-in-error"); //TODO find accurate value
            case "not-done" : vaccinationReported.setCompletionStatus("not-done");  //TODO find accurate value
        }

        //vaccinationReported.setActionCode();
        vaccinationReported.setRefusalReasonCode(i.getReasonCodeFirstRep().getText());

        vaccinationReported.setVaccineCvxCode(i.getVaccineCode().getCodingFirstRep().getCode());

        VaccinationMaster vaccinationMaster = vaccinationReported.getVaccination();

        vaccinationMaster.setAdministeredDate(vaccinationReported.getAdministeredDate());
        vaccinationMaster.setVaccinationId(vaccinationReported.getVaccinationReportedId());
        vaccinationMaster.setVaccinationReported(vaccinationReported);
        vaccinationMaster.setVaccineCvxCode(vaccinationReported.getVaccineCvxCode());
    }

    public static void orgLocationFromFhirImmunization(OrgLocation orgLocation, Immunization i){
        Location l = i.getLocationTarget();
        orgLocation.setOrgFacilityCode(l.getId()); //TODO create an external identifier or change the usage of the name
        orgLocation.setOrgFacilityName(l.getName());
        //orgLocation.setLocationType(l.getTypeFirstRep());
        orgLocation.setAddressCity(l.getAddress().getLine().get(0).getValueNotNull());
        if (l.getAddress().getLine().size() > 1) {
            orgLocation.setAddressLine2(l.getAddress().getLine().get(1).getValueNotNull());
        }
        orgLocation.setAddressCity(l.getAddress().getCity());
        orgLocation.setAddressState(l.getAddress().getState());
        orgLocation.setAddressZip(l.getAddress().getPostalCode());
        orgLocation.setAddressCountry(l.getAddress().getCountry());
    }

}
