//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.talend.dataprep.api.preparation.Preparation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.restassured.response.Response;

/**
 *
 */
public class SearchAPITest extends ApiServiceTestBase {

    @Test
    public void shouldSearch() throws Exception {

        // given
        folderRepository.addFolder("/beer");
        folderRepository.addFolder("/beer/Queue de charrue");
        folderRepository.addFolder("/beer/Saint Feuillien");

        folderRepository.addFolder("/whisky");
        folderRepository.addFolder("/whisky/McCallan Sherry Oak");
        folderRepository.addFolder("/whisky/McCallan Fine Oak");
        folderRepository.addFolder("/whisky/McCallan 1824 Collection");

        final String expectedDSId = createDataset("dataset/dataset.csv", "MacCallan collection", "text/csv");
        createDataset("dataset/dataset.csv", "Saint Feuillien", "text/csv");

        final String expectedPrepId = createPreparationFromFile("dataset/dataset.csv", "cleanup MacCallan", "text/csv", "/whisky");
        createPreparationFromFile("dataset/dataset.csv", "cleanup Queue 2 charrue", "text/csv", "/beer");

        // when
        final Response response = given() //
                .queryParam("name", "callan") //
                .when()//
                .expect().statusCode(200).log().ifError() //
                .get("/api/search");

        // then
        assertThat(response.getStatusCode(), is(200));

        final JsonNode rootNode = mapper.readTree(response.asInputStream());

        assertTrue(rootNode.has("folders"));
        final JsonNode folders = rootNode.get("folders");
        assertTrue(folders.isArray());
        assertEquals(3, folders.size());

        assertTrue(rootNode.has("datasets"));
        final JsonNode datasets = rootNode.get("datasets");
        assertTrue(datasets.isArray());
        assertEquals(1, datasets.size());
        assertEquals(expectedDSId, datasets.get(0).get("id").asText());

        assertTrue(rootNode.has("preparations"));
        final JsonNode preparations = rootNode.get("preparations");
        assertTrue(preparations.isArray());
        assertEquals(1, preparations.size());
        final JsonNode preparation = preparations.get(0);
        assertEquals(expectedPrepId, preparation.get("id").asText());
        assertTrue(preparation.has("folder"));
    }


    @Test
    public void shouldReturnMatchingPreparationsWhenPerformingInventory() throws IOException {
        // given
        folderRepository.addFolder("/");
        final String preparationId = createPreparationFromFile("t-shirt_100.csv", "testInventoryOfPreparations", "text/csv", "/");

        // when
        final Response response = given() //
                .queryParam("name", "Inventory") //
                .expect().statusCode(200).log().ifError() //
                .get("/api/search");

        // then
        assertEquals(200, response.getStatusCode());
        JsonNode rootNode = mapper.readTree(response.asInputStream());
        JsonNode preparations = rootNode.get("preparations");
        List<Preparation> preparationList = mapper.readValue(preparations.toString(), new TypeReference<List<Preparation>>(){});
        assertThat(preparationList.size(), is(1));
        assertEquals("testInventoryOfPreparations", preparationList.get(0).getName());
        assertEquals(preparationId, preparationList.get(0).id());
    }

    @Test
    public void shouldReturnMatchingPreparationsWithSpaceWhenPerformingInventory() throws IOException {
        // given
        folderRepository.addFolder("/");
        final String preparationId = createPreparationFromFile("t-shirt_100.csv", "testInventory OfPreparations", "text/csv", "/");

        // when
        final Response response = given() //
                .queryParam("name", "Inventory ") //
                .expect().statusCode(200).log().ifError() //
                .get("/api/search");

        // then
        assertEquals(200, response.getStatusCode());
        JsonNode rootNode = mapper.readTree(response.asInputStream());
        JsonNode preparations = rootNode.get("preparations");
        List<Preparation> preparationList = mapper.readValue(preparations.toString(), new TypeReference<List<Preparation>>(){});
        assertThat(preparationList.size(), is(1));
        assertEquals("testInventory OfPreparations", preparationList.get(0).getName());
        assertEquals(preparationId, preparationList.get(0).id());
    }

    @Test
    public void shouldNotReturnNonMatchingPreparationsWhenPerformingInventory() throws IOException {
        // given
        createPreparationFromFile("t-shirt_100.csv", "nonMatchingPreparation", "text/csv");

        // when
        final Response response = given() //
                .queryParam("name", "Inventory") //
                .when()
                .expect().statusCode(200).log().ifError() //
                .get("/api/search");

        // then
        assertEquals(200, response.getStatusCode());
        JsonNode rootNode = mapper.readTree(response.asInputStream());
        JsonNode preparations = rootNode.get("preparations");
        List<Preparation> preparationList = mapper.readValue(preparations.toString(), new TypeReference<List<Preparation>>(){});
        assertThat(preparationList.size(), is(0));
    }
}