package org.talend.dataprep.util;

import static org.junit.Assert.*;
import static org.talend.dataprep.util.SortAndOrderHelper.Order;
import static org.talend.dataprep.util.SortAndOrderHelper.Sort;
import static org.talend.dataprep.util.SortAndOrderHelper.Order.ASC;
import static org.talend.dataprep.util.SortAndOrderHelper.Order.DESC;
import static org.talend.dataprep.util.SortAndOrderHelper.Sort.*;

import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.junit.Test;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.share.Owner;
import org.talend.dataprep.dataset.service.UserDataSetMetadata;
import org.talend.dataprep.preparation.service.UserPreparation;

public class SortAndOrderHelperTest {


    @Test
    public void getSortPropertyEditor() {
        PropertyEditor sortPropertyEditor = SortAndOrderHelper.getSortPropertyEditor();
        sortPropertyEditor.setAsText(NAME.camelName());
        Object value = sortPropertyEditor.getValue();
        assertEquals(NAME, value);
    }


    @Test
    public void getPreparationComparator_byName() throws Exception {
        assertTrue(getPreparationComparisonByName("aaa", "bbb", ASC) < 0);
        assertTrue(getPreparationComparisonByName("aaa", "bbb", DESC) > 0);
    }

    @Test
    public void getPreparationComparator_byNameUppercaseShouldNotInfluence() throws Exception {
        assertTrue(getPreparationComparisonByName("AAA", "bbb", ASC) < 0);
        assertTrue(getPreparationComparisonByName("aaa", "BBB", ASC) < 0);
        assertTrue(getPreparationComparisonByName("AAA", "bbb", DESC) > 0);
        assertTrue(getPreparationComparisonByName("aaa", "BBB", DESC) > 0);
    }

    @Test
    public void getPreparationComparator_byDatasetName() throws Exception {
        assertTrue(getPreparationComparisonByDatasetName("aaa", "bbb", ASC) < 0);
        assertTrue(getPreparationComparisonByDatasetName("aaa", "bbb", DESC) > 0);
    }

    @Test
    public void getPreparationComparator_byCreation() throws Exception {
        assertTrue(getPreparationComparisonByCreation(1, 2, ASC) < 0);
        assertTrue(getPreparationComparisonByCreation(1, 2, DESC) > 0);
    }

    @Test
    public void getPreparationComparator_byModification() throws Exception {
        assertTrue(getPreparationComparisonByModification(1, 2, ASC) < 0);
        assertTrue(getPreparationComparisonByModification(1, 2, DESC) > 0);
    }

    @Test
    public void getPreparationComparator_byAuthor() throws Exception {
        assertTrue(getPreparationComparisonByAuthor("aaa", "bbb", ASC) < 0);
        assertTrue(getPreparationComparisonByAuthor("aaa", "bbb", DESC) > 0);
    }

    @Test
    public void getPreparationComparator_bySize() throws Exception {
        assertTrue(getPreparationComparisonBySize(1, 2, ASC) < 0);
        assertTrue(getPreparationComparisonBySize(1, 2, DESC) > 0);
    }

    @Test
    public void getDataSetMetadataComparator_byName() throws Exception {
        assertTrue(getDatasetComparisonByName("aaa", "bbb", ASC) < 0);
        assertTrue(getDatasetComparisonByName("aaa", "bbb", DESC) > 0);
    }

    @Test
    public void getDataSetMetadataComparator_byCreation() throws Exception {
        assertTrue(getDatasetComparisonByCreation(1, 2, ASC) < 0);
        assertTrue(getDatasetComparisonByCreation(1, 2, DESC) > 0);
    }

    @Test
    public void getDataSetMetadataComparator_byModification() throws Exception {
        assertTrue(getDatasetComparisonByModification(1, 2, ASC) < 0);
        assertTrue(getDatasetComparisonByModification(1, 2, DESC) > 0);
    }

    @Test
    public void getDataSetMetadataComparator_byAuthor() throws Exception {
        assertTrue(getDatasetComparisonByAuthor("aaa", "bbb", ASC) < 0);
        assertTrue(getDatasetComparisonByAuthor("aaa", "bbb", DESC) > 0);
    }

    @Test
    public void getDataSetMetadataComparator_bySize() throws Exception {
        assertTrue(getDatasetComparisonBySize(1, 2, ASC) < 0);
        assertTrue(getDatasetComparisonBySize(1, 2, DESC) > 0);
    }

    private int getPreparationComparisonByName(String firstName, String secondName, Order order) {
        return getPreparationComparison(firstName, secondName, null, null, 0, 0, 0, 0, 0, 0, null, null, NAME, order);
    }

    private int getPreparationComparisonByAuthor(String firstAuthor, String secondAuthor, Order order) {
        return getPreparationComparison(null, null, firstAuthor, secondAuthor, 0, 0, 0, 0, 0, 0, null, null, AUTHOR, order);
    }

    private int getPreparationComparisonByCreation(long firstCreation, long secondCreation, Order order) {
        return getPreparationComparison(null, null, null, null, firstCreation, secondCreation, 0, 0, 0, 0, null, null,
                CREATION_DATE,
                order);
    }

    private int getPreparationComparisonByModification(int firstModification, int secondModification, Order order) {
        return getPreparationComparison(null, null, null, null, 0, 0, firstModification, secondModification, 0, 0, null, null,
                LAST_MODIFICATION_DATE, order);
    }

    private int getPreparationComparisonBySize(int firstSize, int secondSize, Order order) {
        return getPreparationComparison(null, null, null, null, 0, 0, 0, 0, firstSize, secondSize, null, null, NB_STEPS, order);
    }

    private int getPreparationComparisonByDatasetName(String firstDsName, String secondDsName, Order order) {
        return getPreparationComparison(null, null, null, null, 0, 0, 0, 0, 0, 0, firstDsName, secondDsName, DATASET_NAME, order);
    }

    private int getPreparationComparison(String firstName, String secondName, //
            String firstAuthor, String secondAuthor, //
            long firstCreation, long secondCreation, //
            long firstModification, long secondModification, //
            long firstSize, long secondSize, //
            String firstDsName, String secondDsName, //
            Sort sort, Order order) {
        String firstDsId = "firstDsId";
        Preparation firstPrep = createPreparation(firstName, firstAuthor, firstCreation, firstModification, firstSize, firstDsId);
        DataSetMetadata firstDs = new DataSetMetadata(firstDsId, firstDsName, null, 0, 0, null, null);

        String secondDsId = "secondDsId";
        Preparation secondPrep = createPreparation(secondName, secondAuthor, secondCreation, secondModification, secondSize,
                secondDsId);
        DataSetMetadata secondDs = new DataSetMetadata(secondDsId, secondDsName, null, 0, 0, null, null);

        // when
        Comparator<Preparation> preparationComparator = SortAndOrderHelper.getPreparationComparator(sort, order,
                p -> Objects.equals(p.getDataSetId(), firstDsId) ? firstDs
                        : (Objects.equals(p.getDataSetId(), secondDsId) ? secondDs : null));

        // then
        assertNotNull(preparationComparator);
        return preparationComparator.compare(firstPrep, secondPrep);
    }

    private int getDatasetComparisonByName(String firstName, String secondName, Order order) {
        return getDatasetComparison(firstName, secondName, null, null, "LastName", "LastName", 0, 0, 0, 0, 0, 0, NAME, order);
    }

    private int getDatasetComparisonByAuthor(String firstAuthor, String secondAuthor, Order order) {
        return getDatasetComparison(null, null, firstAuthor, secondAuthor, "", "", 0, 0, 0, 0, 0, 0, AUTHOR, order);
    }

    private int getDatasetComparisonByCreation(long firstCreation, long secondCreation, Order order) {
        return getDatasetComparison(null, null, null, null, "LastName", "LastName", firstCreation, secondCreation, 0, 0, 0, 0, CREATION_DATE, order);
    }

    private int getDatasetComparisonByModification(int firstModification, int secondModification, Order order) {
        return getDatasetComparison(null, null, null, null, "LastName", "LastName", 0, 0, firstModification, secondModification, 0, 0,
                LAST_MODIFICATION_DATE,
                order);
    }

    private int getDatasetComparisonBySize(int firstSize, int secondSize, Order order) {
        return getDatasetComparison(null, null, null, null, "LastName", "LastName", 0, 0, 0, 0, firstSize, secondSize, NB_RECORDS, order);
    }

    private int getDatasetComparison(String firstName, String secondName, //
                                     String firstOwnerFirstName, String secondOwnerFirstName, //
                                     String firstOwnerLastName, String secondOwnerLastName, //
                                     long firstCreation, long secondCreation, //
                                     long firstModification, long secondModification, //
                                     long firstSize, long secondSize, //
                                     Sort sort, Order order) {
        DataSetMetadata firstDs = createUserDatasetMetadata("firstDsId", firstName, "1", firstCreation, firstModification, null,
                null, firstOwnerFirstName, firstOwnerLastName);
        firstDs.getContent().setNbRecords(firstSize);
        DataSetMetadata secondDs = createUserDatasetMetadata("secondDsId", secondName, "2", secondCreation, secondModification,
                null, null, secondOwnerFirstName, secondOwnerLastName);
        secondDs.getContent().setNbRecords(secondSize);

        // when
        Comparator<DataSetMetadata> preparationComparator = SortAndOrderHelper.getDataSetMetadataComparator(sort, order);

        // then
        assertNotNull(preparationComparator);
        return preparationComparator.compare(firstDs, secondDs);
    }

    private DataSetMetadata createUserDatasetMetadata(String id, String name, String author, long creationDate, long lastModificationDate,
                                                      RowMetadata rowMetadata, String appVersion, String ownerName,
                                                      String ownerLastName) {
        UserDataSetMetadata metadata = new UserDataSetMetadata();
        metadata.setId(id);
        metadata.setName(name);
        metadata.setAuthor(author);
        metadata.setCreationDate(creationDate);
        metadata.setLastModificationDate(lastModificationDate);
        metadata.setRowMetadata(rowMetadata);
        metadata.setAppVersion(appVersion);
        metadata.setOwner(new Owner(id, ownerName, ownerLastName));
        return metadata;
    }

    private Preparation createPreparation(String name, String author, long creation, long modification, long size, String dsId) {
        UserPreparation firstPrep = new UserPreparation();
        firstPrep.setDataSetId(dsId);
        firstPrep.setName(name);
        firstPrep.setAuthor("1234");
        firstPrep.setOwner(new Owner("1234", author, ""));
        firstPrep.setCreationDate(creation);
        firstPrep.setLastModificationDate(modification);
        List<Step> steps = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            steps.add(null);
        }
        firstPrep.setSteps(steps);
        return firstPrep;
    }
}
