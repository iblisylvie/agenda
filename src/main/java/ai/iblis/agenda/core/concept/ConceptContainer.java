// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.core.concept;

import ai.iblis.agenda.Concept;
import ai.iblis.agenda.EntryType;
import org.apache.commons.lang3.StringUtils;
import utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO(qli): split concept declaration and definition.
 *
 * @author Qian Li, <qli@leyantech.com>
 * @date 2016-11-22
 */
public class ConceptContainer {

  private static final Logger LOG = new Logger(ConceptContainer.class);

  private Map<String, Concept> conceptIndexer = new HashMap<>();

  public ConceptContainer(List<Concept> conceptList) {
    fromPb(conceptList);
  }

  private void fromPb(List<Concept> conceptList) {
    conceptList.forEach(concept -> {
      conceptIndexer.put(getName(concept), concept);
    });
  }

  public List<Concept> toPb() {
    return new ArrayList<>(conceptIndexer.values());
  }

  public Concept getConcept(String conceptName) {
    return conceptIndexer.get(conceptName);
  }

  private String getName(Concept concept) {
    String name = concept.getSlot().getName();
    if (StringUtils.isEmpty(name)) {
      LOG.fatal("concept name are not allowed to be empty or null");
    }
    return name;
  }

  private void setConcept(Concept concept) {
    conceptIndexer.put(getName(concept), concept);
  }

  public boolean updateConcept(Concept newConcept) {
    String name = newConcept.getSlot().getName();
    Concept oldConcept = getConcept(name);
    if (oldConcept == null) {
      return false;
    }
    Concept.Builder oldConceptBuilder = oldConcept.toBuilder();
    EntryType oldT = getType(oldConcept);
    EntryType newT = getType(newConcept);
    if (!oldT.equals(newT)) {
      LOG.warn("unmatched concept type, old: {}, new: {}, do not update concept", oldConcept,
          newConcept);
      return false;
    }
    String newConceptNormValue = getNormValue(newConcept);
    String oldConceptNormValue = getNormValue(oldConcept);
    LOG.info("Update the norm value of concept {} from {} to {}.", name,
        oldConceptNormValue.replaceAll("\n", " "), newConceptNormValue.replaceAll("\n", " "));
    oldConceptBuilder.clear();
    oldConceptBuilder.mergeFrom(newConcept);
    oldConceptBuilder.setUpdatedTime(System.currentTimeMillis());
    setConcept(oldConceptBuilder.build());
    return true;
  }

  private EntryType getType(Concept concept) {
    if (concept == null) {
      return null;
    }
    return concept.getSlot().getType();
  }

  public static String getNormValue(Concept concept) {
    if (concept == null) {
      return null;
    }
    return concept.getSlot().getNormedValue();
  }

  public String getNormValue(String conceptName) {
    Concept concept = getConcept(conceptName);
    return getNormValue(concept);
  }

  public Map<String, Concept> getAllConcepts() {
    return conceptIndexer;
  }

  public void clear() {
    conceptIndexer.keySet().forEach(conceptName -> clear(conceptName));
  }

  public void clear(String conceptName) {
    Concept concept = conceptIndexer.get(conceptName);
    if (concept == null) {
      return;
    }
    Concept.Builder conceptBuilder = concept.toBuilder();
    conceptBuilder.getSlotBuilder().setValue("").setNormedValue("");
    conceptIndexer.put(conceptName, conceptBuilder.build());
  }
}
