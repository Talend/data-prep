package org.talend.dataprep.preparation.store;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.api.preparation.Step;

public class PersistentPreparationTest {

    @Test
    public void should_merge_from_other() {
        PersistentPreparation source = new PersistentPreparation();
        source.setId("#4837");

        PreparationDTO theOtherOne = new PreparationDTO();
        theOtherOne.setAuthor("Joss Stone");
        theOtherOne.setCreationDate(source.getCreationDate() - 1000);
        theOtherOne.setDataSetId("ds#123456");
        theOtherOne.setDataSetName("My Dataset Name");
        theOtherOne.setLastModificationDate(theOtherOne.getCreationDate() + 12345682);
        theOtherOne.setName("my preparation name");
        theOtherOne.setHeadId(Step.ROOT_STEP.id());

        PersistentPreparation actual = source.merge(theOtherOne);

        assertEquals("Joss Stone", actual.getAuthor());
        assertEquals(source.getCreationDate() - 1000, actual.getCreationDate());
        assertEquals("ds#123456", actual.getDataSetId());
        assertEquals("My Dataset Name", actual.getDataSetName());
        assertEquals(theOtherOne.getCreationDate() + 12345682, actual.getLastModificationDate());
        assertEquals("my preparation name", actual.getName());
        assertEquals(Step.ROOT_STEP.id(), actual.getHeadId());
    }

    @Test
    public void should_merge_from_source() {
        PreparationDTO theOtherOne = new PreparationDTO();
        theOtherOne.setId("#23874");

        PersistentPreparation source = new PersistentPreparation();
        source.setId("#158387");
        source.setAuthor("Bloc Party");
        source.setCreationDate(theOtherOne.getCreationDate() - 1000);
        source.setDataSetId("ds#65478");
        source.setDataSetName("My Dataset Name");
        source.setLastModificationDate(source.getCreationDate() + 2658483);
        source.setName("banquet");
        source.setHeadId(Step.ROOT_STEP.id());

        PersistentPreparation actual = source.merge(theOtherOne);

        assertEquals("#23874", actual.getId());
        assertEquals("Bloc Party", actual.getAuthor());
        assertEquals("ds#65478", actual.getDataSetId());
        assertEquals("My Dataset Name", actual.getDataSetName());
    }

}