package org.immregistries.iis.kernal.model;

import java.util.HashSet;
import java.util.Set;

public enum ProcessingFlavor {
                              LIME("Lime", "Missing non-critical required fields"),
                              COCONUT("Coconut", "Forecast never returned"),
                              ORANGE("Orange", "Z32 is returned for query matches"),
                              LEMON("Lemon", "Vaccinations are randomly not returned"),
                              GREEN("Green", "Typhoid vaccination is never returned"),
                              CRANBERRY("Cranberry",
                                  "Vaccination update is not accepted without at least one vaccination reported"),
                              BILBERRY("Bilberry",
                                  "Vaccination update is not accepted without at least one vaccination or refusal reported"),
                              PEAR("Pear",
                                  "The administered-at-location will not be recognized if valued and the message rejected"),
                              BLACKBERRY("Blackberry", "The patient's address is required"),
                              SPRUCE("Spruce",
                                  "The RXR segment is required for all administered vaccinations"),
                              CANTALOUPE("Cantaloupe", "Dates will not be strictly parsed"),
                              CLEMENTINE("Clementine",
                                  "Will accept vaccination given before date of birth"),
                              ELDERBERRIES("Elderberries",
                                  "Will accept invalid sex codes and valid sex codes other than M, F, or U"),
                              GUAVA("Guava",
                                  "Will accept invalid state or country codes in address"),
                              FIG("Fig",
                                  "Will not accept invalid or unrecognized race or ethnicity codes"),
                              PITAYA("Pitaya",
                                  "Will return race and ethnicity in RSP as it was accepted"),
                              PERSIMMON("Persimmon",
                                  "Will return race and ethnicity in RSP, if it is valid"),
                              PLANTAIN("Plantain",
                                  "Will accept invalid multiple birth indicator or birth orders"),
                              QUINZE("Quinze",
                                  "Will not accept phone numbers unless they are of Telecommunication Use Code PRN"),
                              SOURSOP("Soursop",
                                  "The facility submitted in MSH-4 must match what is sent in the CDC SOAP WSDL request"),
                              ICE("Ice", "Forecast generated with open-cds ICE"),
                              PHI("PHI", "Trailing \"AIRA\" is removed from patient names 10% of the time on RSPs"),
                              SNAIL("Snail",
                                  "Patients are not returned until 0, 30, 60, or 90 seconds after they have been submitted"),
                              SNAIL30("Snail30",
                                  "Patients are not returned until 30 seconds after they have been submitted"),
                              SNAIL60("Snail60",
                                  "Patients are not returned until 60 seconds after they have been submitted"),
                              SNAIL90("Snail90",
                                  "Patients are not returned until 90 seconds after they have been submitted");

  private String key = "";
  private String behaviorDescription = "";

  private ProcessingFlavor(String key, String behaviorDescription) {
    this.key = key;
    this.behaviorDescription = behaviorDescription;
  }

  public String getKey() {
    return key;
  }

  public static Set<ProcessingFlavor> getProcessingStyle(String label) {
    Set<ProcessingFlavor> processingFlavorSet = new HashSet<>();
    if (label != null) {
      label = label.toUpperCase();
      for (ProcessingFlavor ps : ProcessingFlavor.values()) {
        String key = ps.key.toUpperCase();
        if (label.startsWith(key + " ") || label.endsWith(" " + key)
            || label.indexOf(" " + key + " ") > 0) {
          processingFlavorSet.add(ps);
        }
      }
    }
    return processingFlavorSet;
  }

  public String getBehaviorDescription() {
    return behaviorDescription;
  }

}
