//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.service;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.preparation.Preparation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.restassured.response.Response;

public class SearchAPITest extends ApiServiceTestBase {

    @Test
    public void shouldReturnMatchingPreparationsWhenPerformingInventory() throws IOException {
        // given
        final String preparationId = testClient.createPreparationFromFile("t-shirt_100.csv", "testInventoryOfPreparations",
                folderRepository.getHome().getId());

        // when
        final Response response = given() //
                .queryParam("name", "Inventory") //
                .expect().statusCode(200).log().ifError() //
                .get("/api/search");

        // then
        assertEquals(200, response.getStatusCode());
        JsonNode rootNode = mapper.readTree(response.asInputStream());
        JsonNode preparations = rootNode.get("preparations");
        List<Preparation> preparationList = mapper.readValue(preparations.toString(), new TypeReference<List<Preparation>>() {

        });
        assertThat(preparationList.size(), is(1));
        assertEquals("testInventoryOfPreparations", preparationList.get(0).getName());
        assertEquals(preparationId, preparationList.get(0).id());
    }

    @Test
    public void shouldReturnMatchingPreparationsWithSpaceWhenPerformingInventory() throws IOException {
        // given
        final String preparationId = testClient.createPreparationFromFile("t-shirt_100.csv", "testInventory OfPreparations",
                folderRepository.getHome().getId());

        // when
        final Response response = given() //
                .queryParam("name", "Inventory ") //
                .expect().statusCode(200).log().ifError() //
                .get("/api/search");

        // then
        assertEquals(200, response.getStatusCode());
        JsonNode rootNode = mapper.readTree(response.asInputStream());
        JsonNode preparations = rootNode.get("preparations");
        List<Preparation> preparationList = mapper.readValue(preparations.toString(), new TypeReference<List<Preparation>>() {

        });
        assertThat(preparationList.size(), is(1));
        assertEquals("testInventory OfPreparations", preparationList.get(0).getName());
        assertEquals(preparationId, preparationList.get(0).id());
    }

    @Test
    public void shouldNotReturnNonMatchingPreparationsWhenPerformingInventory() throws IOException {
        // given
        testClient.createPreparationFromFile("t-shirt_100.csv", "nonMatchingPreparation", home.getId());

        // when
        final Response response = given() //
                .queryParam("name", "Inventory") //
                .when().expect().statusCode(200).log().ifError() //
                .get("/api/search");

        // then
        assertEquals(200, response.getStatusCode());
        JsonNode rootNode = mapper.readTree(response.asInputStream());
        JsonNode preparations = rootNode.get("preparations");
        List<Preparation> preparationList = mapper.readValue(preparations.toString(), new TypeReference<List<Preparation>>() {

        });
        assertThat(preparationList.size(), is(0));
    }

    @Test
    public void shouldSearch() throws Exception {
        // given
        Folder beerFolder = folderRepository.addFolder(home.getId(), "/beer");
        folderRepository.addFolder(home.getId(), "/beer/Queue de charrue");
        folderRepository.addFolder(home.getId(), "/beer/Saint Feuillien");

        Folder whiskyFolder = folderRepository.addFolder(home.getId(), "/whisky");
        folderRepository.addFolder(home.getId(), "/whisky/McCallan Sherry Oak");
        folderRepository.addFolder(home.getId(), "/whisky/McCallan Fine Oak");
        folderRepository.addFolder(home.getId(), "/whisky/McCallan 1824 Collection");

        Folder menuFolder = folderRepository.addFolder(home.getId(), "/menu");
        folderRepository.addFolder(home.getId(), "/menu/menu A");
        folderRepository.addFolder(home.getId(), "/menu/menu B");
        folderRepository.addFolder(home.getId(), "/menu/menu C");

        final String datasetId1 = testClient.createDataset("dataset/dataset.csv", "MacCallan collection");
        final String datasetId2 = testClient.createDataset("dataset/dataset.csv", "menu");
        testClient.createDataset("dataset/dataset.csv", "Saint Feuillien");
        testClient.createDataset("dataset/dataset.csv", "menu bis");

        final String preparationId1 = testClient.createPreparationFromFile("dataset/dataset.csv", "cleanup MacCallan",
                whiskyFolder.getId());
        final String preparationId2 = testClient.createPreparationFromFile("dataset/dataset.csv", "menu", menuFolder.getId());
        testClient.createPreparationFromFile("dataset/dataset.csv", "cleanup Queue 2 charrue", beerFolder.getId());
        testClient.createPreparationFromFile("dataset/dataset.csv", "cleanup menu", menuFolder.getId());

        final boolean nonStrict = false;
        final boolean strict = true;

        // when / then
        assertSearch("callan", nonStrict,
                new String[] { "/whisky/McCallan Sherry Oak", "/whisky/McCallan Fine Oak", "/whisky/McCallan 1824 Collection" },
                new String[] { datasetId1 }, new String[] { preparationId1 });

        assertSearch("menu", strict, new String[] { "/menu" }, new String[] { datasetId2 }, new String[] { preparationId2 });
    }

    private void assertSearch(final String name, final boolean strict, final String[] expectedFoldersPath,
                              final String[] expectedDatasetsId, final String[] expectedPreparationsId) throws IOException {
        // when
        final Response response = given() //
                .queryParam("name", name) //
                .queryParam("strict", strict) //
                .when()//
                .expect().statusCode(200).log().ifError() //
                .get("/api/search");

        // then
        assertThat(response.getStatusCode(), is(200));

        final JsonNode rootNode = mapper.readTree(response.asInputStream());

        assertSearchItems(rootNode, "folders", "path", expectedFoldersPath);
        assertSearchItems(rootNode, "datasets", "id", expectedDatasetsId);
        assertSearchItems(rootNode, "preparations", "id", expectedPreparationsId);

        final JsonNode preparations = rootNode.get("preparations");
        for (int i = 0; i < preparations.size(); ++i) {
            assertTrue(preparations.get(i).has("folder"));
        }
    }

    private void assertSearchItems(final JsonNode rootNode, final String prop, final String field,
                                   final String[] expectedFields) {
        // check that the property holding the list of items exists
        assertTrue(rootNode.has(prop));

        // check that the number of items is the expected number
        final JsonNode items = rootNode.get(prop);
        assertTrue(items.isArray());
        assertThat(items.size(), is(expectedFields.length));

        // check that list of items contains exactly list of expected ones
        // by comparing the "field" of each item with the expectedFields
        final List<String> extractedFields = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); ++i) {
            extractedFields.add(items.get(i).get(field).textValue());
        }
        assertThat(extractedFields, containsInAnyOrder(expectedFields));
    }
}
