package uk.ac.ox.ctsu.arts.fhirservice.model;


import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MTSOrganisation {
    String name;
    String parentOrganisationId;
}