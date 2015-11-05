package org.talend.dataprep.preparation.service;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.util.MatcherAssertionErrors.assertThat;
import static org.talend.dataprep.api.preparation.Step.ROOT_STEP;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.talend.dataprep.api.preparation.*;
import org.talend.dataprep.preparation.Application;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class PreparationServiceTest {

    @Value("${local.server.port}")
    public int port;

    @Autowired
    private PreparationRepository repository;

    @Autowired
    private Jackson2ObjectMapperBuilder builder;

    @Before
    public void setUp() {
        RestAssured.port = port;
    }

    @After
    public void tearDown() throws Exception {
        repository.clear();
    }

    @Test
    public void CORSHeaders() throws Exception {
        when().get("/preparations").then().header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT")
                .header("Access-Control-Max-Age", "3600")
                .header( "Access-Control-Allow-Headers", "x-requested-with, Content-Type" );
    }

    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------GETTER------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    @Test
    public void should_list_all_preparations() throws Exception {
        //given
        when().get("/preparations/all").then().statusCode(HttpStatus.OK.value()).body(sameJSONAs("[]"));

        final Preparation preparation = new Preparation("1234", ROOT_STEP);
        preparation.setCreationDate(0);
        preparation.setLastModificationDate(12345);
        repository.add(preparation);

        //when
        final Response response = when().get("/preparations/all");

        //then
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body(sameJSONAs("[{\"id\":\"ae242b07084aa7b8341867a8be1707f4d52501d1\",\"dataSetId\":\"1234\",\"author\":null,\"name\":null,\"creationDate\":0,\"lastModificationDate\":12345,\"steps\":[\"f6e172c33bdacbc69bca9d32b2bd78174712a171\"],\"diff\":[],\"actions\":[],\"metadata\":[]}]"));

        //given
        final Preparation preparation1 = new Preparation("5678", ROOT_STEP);
        preparation1.setCreationDate(500);
        preparation1.setLastModificationDate(456789);
        repository.add(preparation1);

        //when
        final Response responseMultiple = when().get("/preparations/all");

        //then
        responseMultiple.then()
                .statusCode(HttpStatus.OK.value())
                .body(sameJSONAs(
                        "[{\"id\":\"ae242b07084aa7b8341867a8be1707f4d52501d1\",\"dataSetId\":\"1234\",\"author\":null,\"name\":null,\"creationDate\":0,\"lastModificationDate\":12345,\"steps\":[\"f6e172c33bdacbc69bca9d32b2bd78174712a171\"],\"diff\":[],\"actions\":[],\"metadata\":[]}, {\"id\":\"1de0ffaa4e00437dd0c7e1097caf5e5657440ee5\",\"dataSetId\":\"5678\",\"author\":null,\"name\":null,\"creationDate\":500,\"lastModificationDate\":456789,\"steps\":[\"f6e172c33bdacbc69bca9d32b2bd78174712a171\"],\"diff\":[],\"actions\":[],\"metadata\":[]}]")
                        .allowingAnyArrayOrdering());
    }

    @Test
    public void list() throws Exception {
        when().get("/preparations").then().statusCode(HttpStatus.OK.value()).body(sameJSONAs("[]"));
        repository.add(new Preparation("1234", ROOT_STEP));
        when().get("/preparations").then().statusCode(HttpStatus.OK.value())
                .body(sameJSONAs("[\"ae242b07084aa7b8341867a8be1707f4d52501d1\"]"));
        repository.add(new Preparation("5678", ROOT_STEP));
        List<String> list = when().get("/preparations").jsonPath().getList("");
        assertThat(list, hasItems("ae242b07084aa7b8341867a8be1707f4d52501d1", "1de0ffaa4e00437dd0c7e1097caf5e5657440ee5"));
    }

    @Test
    public void get() throws Exception {
        Preparation preparation = new Preparation("1234", ROOT_STEP);
        preparation.setCreationDate( 0 );
        preparation.setLastModificationDate( 12345 );
        repository.add(preparation);
        String preparationDetails = when().get("/preparations/{id}", preparation.id()).asString();
        InputStream expected = PreparationServiceTest.class.getResourceAsStream( "preparation_1234.json" );
        assertThat(preparationDetails, sameJSONAsFile( expected ));
    }

    @Test
    public void cloning() throws Exception {
        Preparation preparation = new Preparation("56789", ROOT_STEP);
        preparation.setName("beer");
        preparation.setCreationDate(1);
        preparation.setLastModificationDate(6789);
        repository.add( preparation );
        String preparationDetails = when().get("/preparations/{id}", preparation.id()).asString();

        InputStream expected = PreparationServiceTest.class.getResourceAsStream( "preparation_beer.json" );
        assertThat(preparationDetails, sameJSONAsFile(expected));

        ObjectMapper objectMapper = new ObjectMapper() //
            .configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE );

        Preparation result = objectMapper.reader( Preparation.class ).readValue(preparationDetails);

        Assertions.assertThat(result).isEqualToComparingOnlyGivenFields(preparation, //
                "name", "lastModificationDate", "creationDate");

        Response cloneResponse =  when().put("/preparations/clone/{id}", preparation.id());

        String cloneId = cloneResponse.asString();

        String preparationDetailsClone = when().get("/preparations/{id}", cloneId).asString();

        Preparation clone = objectMapper.reader( Preparation.class ).readValue(preparationDetailsClone);

        Assertions.assertThat( clone.getCreationDate() ).isGreaterThan( result.getCreationDate() );
        Assertions.assertThat( clone.getName() ).isEqualTo( result.getName() + " Copy" );
        Assertions.assertThat( clone.getId() ).isNotEqualTo( result.getId() );
        Assertions.assertThat( clone.getDataSetId() ).isEqualTo( result.getDataSetId() );

    }

    @Test
    public void should_return_list_of_specific_dataset_preparations() throws Exception {
        // given : relevant preparation
        final String wantedDataSetId = "wanted";
        final String preparation1 = createPreparation(wantedDataSetId, "prep_1");
        final String preparation2 = createPreparation(wantedDataSetId, "prep_2");

        // given : noise data not to be returned by the service
        final String preparation3 = createPreparation("4523", "prep_3");
        final String preparation4 = createPreparation("7534", "prep_4");
        final String preparation5 = createPreparation("1598", "prep_5");

        // when
        final String result = when().get("/preparations?dataSetId=" + wantedDataSetId).asString();
        final List<String> preparationIds = JsonPath.from(result).get("id");

        // then
        assertThat(preparationIds, hasItem(preparation1));
        assertThat(preparationIds, hasItem(preparation2));
        assertThat(preparationIds, not(hasItem(preparation3)));
        assertThat(preparationIds, not(hasItem(preparation4)));
        assertThat(preparationIds, not(hasItem(preparation5)));
    }

    @Test
    public void shouldListErrors() throws Exception {
        // when
        final String errors = when().get("/preparations/errors").asString();

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode actualErrorCodes = mapper.readTree(errors);

        // then
        assertTrue(actualErrorCodes.isArray());
        assertTrue(actualErrorCodes.size() > 0);
        for (final JsonNode currentCode : actualErrorCodes) {
            assertTrue(currentCode.has("code"));
            assertTrue(currentCode.has("http-status-code"));
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------LIFECYCLE----------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    @Test
    public void create() throws Exception {
        assertThat(repository.listAll(Preparation.class).size(), is(0));
        String preparationId = given().contentType(ContentType.JSON).body("{\"name\": \"test_name\", \"dataSetId\": \"1234\"}")
                .when().put("/preparations").asString();
        assertThat(preparationId, is("170e086992df1848b8fc9459d87938af6be78720"));
        assertThat(repository.listAll(Preparation.class).size(), is(1));
        Preparation preparation = repository.listAll(Preparation.class).iterator().next();
        assertThat(preparation.id(), is("170e086992df1848b8fc9459d87938af6be78720"));
        assertThat(preparation.getName(), is("test_name"));
    }

    @Test
    public void createWithSpecialCharacters() throws Exception {
        assertThat(repository.listAll(Preparation.class).size(), is(0));
        String preparationId = given().contentType(ContentType.JSON).body("{\"name\": \"éàçè\", \"dataSetId\": \"1234\"}".getBytes("UTF-8"))
                .when().put("/preparations").asString();
        assertThat(preparationId, is("f7c4550ce8f9e7071c638b71e2930e3dd65ac3c0"));
        assertThat(repository.listAll(Preparation.class).size(), is(1));
        Preparation preparation = repository.listAll(Preparation.class).iterator().next();
        assertThat(preparation.id(), is("f7c4550ce8f9e7071c638b71e2930e3dd65ac3c0"));
        assertThat(preparation.getName(), is("éàçè"));
    }

    @Test
    public void delete() throws Exception {
        assertThat(repository.listAll(Preparation.class).size(), is(0));
        String preparationId = given().contentType(ContentType.JSON).body("{\"name\": \"test_name\", \"dataSetId\": \"1234\"}")
                .when().put("/preparations").asString();
        assertThat(preparationId, is("170e086992df1848b8fc9459d87938af6be78720"));
        assertThat(repository.listAll(Preparation.class).size(), is(1));
        Preparation preparation = repository.listAll(Preparation.class).iterator().next();
        assertThat(preparation.id(), is("170e086992df1848b8fc9459d87938af6be78720"));
        assertThat(preparation.getName(), is("test_name"));

        when().delete("/preparations/{id}", preparationId).then().statusCode(HttpStatus.OK.value());
        assertThat(repository.listAll(Preparation.class).size(), is(0));
    }

    @Test
    public void update() throws Exception {
        assertThat(repository.listAll(Preparation.class).size(), is(0));
        String preparationId = given().contentType(ContentType.JSON).body("{\"name\": \"test_name\", \"dataSetId\": \"1234\"}")
                .when().put("/preparations").asString();
        assertThat(preparationId, is("170e086992df1848b8fc9459d87938af6be78720"));
        Preparation preparation = repository.listAll(Preparation.class).iterator().next();
        long oldModificationDate = preparation.getLastModificationDate();

        // Test preparation details update
        preparationId = given().contentType(ContentType.JSON).body("{\"name\": \"test_name_updated\", \"dataSetId\": \"1234\"}")
                .when().put("/preparations/{id}", preparationId).asString();

        // Preparation id should not change
        assertThat(preparationId, is("0d291a2159ae36ee9177b8b845b3c8f1b0e0f30b"));
        Collection<Preparation> preparations = repository.listAll(Preparation.class);
        assertThat(preparations.size(), is(1));
        preparation = preparations.iterator().next();
        assertThat(preparation.id(), is("0d291a2159ae36ee9177b8b845b3c8f1b0e0f30b"));
        assertThat(preparation.getName(), is("test_name_updated"));
        assertThat(preparation.getLastModificationDate(), is(greaterThan(oldModificationDate)));
    }

    @Test
    public void updateWithSpecialArguments() throws Exception {
        Preparation preparation = new Preparation();
        preparation.setDataSetId("1234");
        preparation.setName("test_name");
        String preparationId = given().contentType(ContentType.JSON).body(builder.build().writer().writeValueAsBytes(preparation))
                .when().put("/preparations").asString();
        assertThat(preparationId, is("170e086992df1848b8fc9459d87938af6be78720"));

        // Test preparation details update
        preparationId = given().contentType(ContentType.JSON).body("{\"name\": \"éàçè\", \"dataSetId\": \"1234\"}".getBytes("UTF-8"))
                .when().put("/preparations/{id}", preparationId).asString();

        // Preparation id should change (new name)
        assertThat(preparationId, is("f7c4550ce8f9e7071c638b71e2930e3dd65ac3c0"));
        Collection<Preparation> preparations = repository.listAll(Preparation.class);
        assertThat(preparations.size(), is(1));
        preparation = preparations.iterator().next();
        assertThat(preparation.id(), is("f7c4550ce8f9e7071c638b71e2930e3dd65ac3c0"));
        assertThat(preparation.getName(), is("éàçè"));
    }

    @Test
    public void should_change_preparation_head() throws Exception {
        //given
        final String preparationId = createPreparation("1234", "my preparation");
        final String firstStepId = applyTransformation(preparationId, "upper_case.json");
        final String secondStepId = applyTransformation(preparationId, "lower_case.json");

        Preparation preparation = repository.get(preparationId, Preparation.class);
        assertThat(preparation.getStep().id(), is(secondStepId));

        //when
        given().when()//
                .put("/preparations/{id}/head/{stepId}", preparationId, firstStepId)//
                .then()//
                .statusCode(200);

        //then
        preparation = repository.get(preparationId, Preparation.class);
        assertThat(preparation.getStep().id(), is(firstStepId));
    }

    @Test
    public void should_throw_exception_on_preparation_head_change_with_unknown_step() throws Exception {
        //given
        final String preparationId = createPreparation("1234", "my preparation");
        final String firstStepId = applyTransformation(preparationId, "upper_case.json");

        Preparation preparation = repository.get(preparationId, Preparation.class);
        assertThat(preparation.getStep().id(), is(firstStepId));

        //when
        final Response response = given().when()//
                .put("/preparations/{id}/head/{stepId}", preparationId, "unknown_step_id");

        //then
        response.then()//
                .statusCode(400)//
                .assertThat()//
                .body("code", is("TDP_PS_PREPARATION_STEP_DOES_NOT_EXIST"));
    }

    //------------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------APPEND STEP----------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    @Test
    public void should_add_action_step_after_head() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");
        Preparation preparation = repository.get(preparationId, Preparation.class);
        final long oldModificationDate = preparation.getLastModificationDate();

        assertThat(preparation.getStep().id(), is(ROOT_STEP.getId()));

        // when
        applyTransformation(preparationId, "copy_lastname.json");

        // then
        final String expectedStepId = "907741a33bba6e7b3c6c2e4e7d1305c6bd0644b8";

        preparation = repository.get(preparation.id(), Preparation.class);
        assertThat(preparation.getStep().id(), is(expectedStepId));
        assertThat(preparation.getLastModificationDate(), is(greaterThan(oldModificationDate)));

        final Step head = repository.get(expectedStepId, Step.class);
        final PreparationActions headAction = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headAction.getActions(), hasSize(1));
        assertThat(headAction.getActions().get(0).getAction(), is("copy"));
    }

    @Test
    public void should_add_action_with_filter_step_after_head() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");
        Preparation preparation = repository.get(preparationId, Preparation.class);
        final long oldModificationDate = preparation.getLastModificationDate();

        assertThat(preparation.getStep().id(), is(ROOT_STEP.getId()));

        // when
        applyTransformation(preparationId, "copy_lastname_filter.json");

        // then
        final String expectedStepId = "1306e2ac2526530ec8cd75206eeaa4191eafe4fa";

        preparation = repository.get(preparation.id(), Preparation.class);
        assertThat(preparation.getStep().id(), is(expectedStepId));
        assertThat(preparation.getLastModificationDate(), is(greaterThan(oldModificationDate)));

        final Step head = repository.get(expectedStepId, Step.class);
        final PreparationActions headAction = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headAction.getActions(), hasSize(1));
        final Action copyAction = headAction.getActions().get(0);
        assertThat(copyAction.getAction(), is("copy"));
        assertThat(copyAction.getParameters().get(ImplicitParameters.FILTER.getKey()), is("{\"eq\":{\"field\":\"0001\",\"value\":\"value\"}}"));
    }

    @Test
    public void should_save_step_diff() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");
        final Preparation preparation = repository.get(preparationId, Preparation.class);

        assertThat(preparation.getStep().id(), is(ROOT_STEP.getId()));

        // when
        applyTransformation(preparationId, "copy_lastname.json");

        // then
        final String expectedStepId = "907741a33bba6e7b3c6c2e4e7d1305c6bd0644b8";
        final Step head = repository.get(expectedStepId, Step.class);
        assertThat(head.getParent(), is(ROOT_STEP.getId()));
        assertThat(head.getDiff().getCreatedColumns(), hasSize(1));
        assertThat(head.getDiff().getCreatedColumns(), hasItem("0006"));
    }

    @Test
    public void should_return_error_when_scope_is_not_consistent_on_append_transformation() throws Exception {
        //given
        final String preparationId = createPreparation("1234", "my preparation");

        //when
        final Response request = given().body(IOUtils.toString(PreparationServiceTest.class.getResourceAsStream("incomplete_transformation_params.json")))//
                .contentType(ContentType.JSON)//
                .when()//
                .post("/preparations/{id}/actions", preparationId);

        //then
        request.then()//
                .statusCode(400)//
                .assertThat()//
                .body("code", is("TDP_ALL_MISSING_ACTION_SCOPE"));
    }

    //------------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------UPDATE STEP----------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    @Test
    public void should_modify_single_action() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");
        final String firstStepId = applyTransformation(preparationId, "upper_case.json");
        assertThat(firstStepId, is("22b9436476d28dcf800dddd11aa7441f20c0c1e9"));

        Preparation preparation = repository.get(preparationId, Preparation.class);
        final long oldModificationDate = preparation.getLastModificationDate();

        // when
        given().body(IOUtils.toString(PreparationServiceTest.class.getResourceAsStream("upper_case_modified.json")))//
                .contentType(ContentType.JSON)//
                .when()//
                .put("/preparations/{id}/actions/{action}", preparationId, firstStepId);

        // then
        final String expectedStepId = "971ff04a46557df17bc1302f2fa2dbcac935086b";
        preparation = repository.get(preparationId, Preparation.class);
        assertThat(preparation.getStep().id(), is(expectedStepId));
        assertThat(preparation.getLastModificationDate(), is(greaterThan(oldModificationDate)));

        final Step head = repository.get(expectedStepId, Step.class);
        assertThat(head.getParent(), is(ROOT_STEP.getId()));
    }

    @Test
    public void should_modify_last_action() throws Exception {
        // Initial preparation
        final String preparationId = createPreparation("1234", "my preparation");
        final String firstStepId = applyTransformation(preparationId, "upper_case.json");
        final String secondStepId = applyTransformation(preparationId, "lower_case.json");
        assertThat(secondStepId, is("3fdaae0ccfd3bfd869790999670106535cdbccf1"));

        Preparation preparation = repository.get(preparationId, Preparation.class);
        final long oldModificationDate = preparation.getLastModificationDate();

        // when : update second (last) step
        given().body(IOUtils.toString(PreparationServiceTest.class.getResourceAsStream("upper_case_modified.json")))
                .contentType(ContentType.JSON).when()
                .put("/preparations/{id}/actions/{action}", preparationId, secondStepId);

        // then
        final String expectedStepId = "3086490ebc8ac72475d249010b0ff67c38ae3454";
        preparation = repository.get(preparationId, Preparation.class);
        assertThat(preparation.getStep().id(), is(expectedStepId));
        assertThat(preparation.getLastModificationDate(), is(greaterThan(oldModificationDate)));

        final Step head = repository.get(expectedStepId, Step.class);
        assertThat(head.getParent(), is(firstStepId));
    }

    @Test
    public void should_modify_first_action() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");
        final String firstStepId = applyTransformation(preparationId, "upper_case.json");
        assertThat(firstStepId, is("22b9436476d28dcf800dddd11aa7441f20c0c1e9"));
        final String secondStepId = applyTransformation(preparationId, "lower_case.json");
        assertThat(secondStepId, is("3fdaae0ccfd3bfd869790999670106535cdbccf1"));

        Preparation preparation = repository.get(preparationId, Preparation.class);
        final long oldModificationDate = preparation.getLastModificationDate();

        // when
        given().body(IOUtils.toString(PreparationServiceTest.class.getResourceAsStream("upper_case_modified.json")))
                .contentType(ContentType.JSON).when()
                .put("/preparations/{id}/actions/{action}", preparation.id(), "a41184275b046d86c8d98d413ed019bc0a7f3c49");

        // then
        final String expectedFirstStepId = "22b9436476d28dcf800dddd11aa7441f20c0c1e9";
        final String expectedSecondStepId = "3fdaae0ccfd3bfd869790999670106535cdbccf1";

        preparation = repository.get(preparationId, Preparation.class);
        assertThat(preparation.getStep().id(), is(expectedSecondStepId));
        assertThat(preparation.getLastModificationDate(), is(greaterThanOrEqualTo(oldModificationDate)));

        final Step head = repository.get(expectedSecondStepId, Step.class);
        assertThat(head.getParent(), is(expectedFirstStepId));

        final Step first = repository.get(expectedFirstStepId, Step.class);
        assertThat(first.getParent(), is(ROOT_STEP.getId()));
    }

    @Test
    public void should_save_updated_step_diff_and_shift_columns_ids() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");
        final String step0 = applyTransformation(preparationId, "update/0.copy_birth.json");
        final String step1 = applyTransformation(preparationId, "update/1.split_2_columns.json");
        final String step2 = applyTransformation(preparationId, "update/2.copy_firstname_after_split.json");
        final String step3 = applyTransformation(preparationId, "update/3.rename_copy_firstname.json");
        final String step4 = applyTransformation(preparationId, "update/4.copy_lastname.json");

        assertThatStepHasCreatedColumns(step0, "0007");
        assertThatStepHasCreatedColumns(step1, "0008", "0009");
        assertThatStepHasCreatedColumns(step2, "0010");
        assertThatStepIsOnColumn(step3, "0010");
        assertThatStepHasCreatedColumns(step4, "0011");

        // when : +1 column
        given().body(IOUtils.toString(PreparationServiceTest.class.getResourceAsStream("update/1bis.split_3_columns.json")))
                .contentType(ContentType.JSON).when()
                .put("/preparations/{id}/actions/{action}", preparationId, step1);

        // then
        final Preparation preparation = repository.get(preparationId, Preparation.class);
        final List<String> stepIds = PreparationUtils.listStepsIds(preparation.getStep(), repository);
        assertThatStepHasCreatedColumns(stepIds.get(1), "0007"); // id < 0009 : do not change
        assertThatStepHasCreatedColumns(stepIds.get(2), "0008", "0009", "0010"); // +1 column
        assertThatStepHasCreatedColumns(stepIds.get(3), "0011"); // id >= 0009 : shift +1
        assertThatStepIsOnColumn(stepIds.get(4), "0011"); // id >= 0009 : shift + 1
        assertThatStepHasCreatedColumns(stepIds.get(5), "0012"); // id >= 0009 : shift + 1
    }

    @Test
    public void should_return_error_when_scope_is_not_consistent_on_transformation_update() throws Exception {
        //given
        final String preparationId = createPreparation("1234", "my preparation");
        final String stepId = applyTransformation(preparationId, "upper_case.json");

        //when
        final Response request = given().body(IOUtils.toString(PreparationServiceTest.class.getResourceAsStream("incomplete_transformation_params.json")))//
                .contentType(ContentType.JSON)//
                .when()//
                .put("/preparations/{id}/actions/{action}", preparationId, stepId);

        //then
        request.then()//
                .statusCode(400)//
                .assertThat()//
                .body("code", is("TDP_ALL_MISSING_ACTION_SCOPE"));
    }

    //------------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------DELETE STEP----------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    @Test
    public void should_delete_single_step() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        final String firstStepId = applyTransformation(preparationId, "upper_case.json");
        final String secondStepId = applyTransformation(preparationId, "lower_case.json");

        Step head = repository.get(secondStepId, Step.class);
        PreparationActions headActions = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headActions.getActions(), hasSize(2));
        assertThat(headActions.getActions().get(0).getAction(), is("uppercase"));
        assertThat(headActions.getActions().get(1).getAction(), is("lowercase"));

        // when : delete second step in single mode
        when().delete("/preparations/{id}/actions/{action}", preparationId, firstStepId)//
                .then()//
                .statusCode(200);

        // then
        final Preparation preparation = repository.get(preparationId, Preparation.class);
        final String headId = preparation.getStep().id();

        head = repository.get(headId, Step.class);
        headActions = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headActions.getActions(), hasSize(1));
        assertThat(headActions.getActions().get(0).getAction(), is("lowercase"));
    }

    @Test
    public void should_delete_steps_that_apply_to_created_column() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        applyTransformation(preparationId, "upper_case.json");
        final String copyStepId = applyTransformation(preparationId, "copy_lastname.json");
        applyTransformation(preparationId, "rename_copy_lastname.json");
        final String headStepId = applyTransformation(preparationId, "lower_case.json");

        Step head = repository.get(headStepId, Step.class);
        PreparationActions headActions = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headActions.getActions(), hasSize(4));
        assertThat(headActions.getActions().get(0).getAction(), is("uppercase"));
        assertThat(headActions.getActions().get(1).getAction(), is("copy"));
        assertThat(headActions.getActions().get(2).getAction(), is("rename_column"));
        assertThat(headActions.getActions().get(3).getAction(), is("lowercase"));

        // when : delete second step in single mode
        when().delete("/preparations/{id}/actions/{action}", preparationId, copyStepId)//
                .then()//
                .statusCode(200);

        // then
        final Preparation preparation = repository.get(preparationId, Preparation.class);
        final String newHeadStepId = preparation.getStep().id();

        head = repository.get(newHeadStepId, Step.class);
        headActions = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headActions.getActions(), hasSize(2));
        assertThat(headActions.getActions().get(0).getAction(), is("uppercase"));
        assertThat(headActions.getActions().get(1).getAction(), is("lowercase"));
    }

    @Test
    public void should_shift_column_created_after_step_with_all_actions_parameters_on_those_steps() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        applyTransformation(preparationId, "upper_case.json");
        final String copyStepId = applyTransformation(preparationId, "copy_lastname.json");
        applyTransformation(preparationId, "rename_copy_lastname.json");
        applyTransformation(preparationId, "copy_firstname.json");
        applyTransformation(preparationId, "rename_copy_firstname.json");
        final String headStepId = applyTransformation(preparationId, "lower_case.json");

        Step head = repository.get(headStepId, Step.class);
        PreparationActions headActions = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headActions.getActions(), hasSize(6));
        assertThat(headActions.getActions().get(0).getAction(), is("uppercase"));
        assertThat(headActions.getActions().get(1).getAction(), is("copy"));
        assertThat(headActions.getActions().get(2).getAction(), is("rename_column"));
        assertThat(headActions.getActions().get(3).getAction(), is("copy"));
        assertThat(headActions.getActions().get(4).getAction(), is("rename_column"));
        assertThat(headActions.getActions().get(5).getAction(), is("lowercase"));

        // when : delete second step in single mode
        when().delete("/preparations/{id}/actions/{action}", preparationId, copyStepId)//
                .then()//
                .statusCode(200);

        // then
        final Preparation preparation = repository.get(preparationId, Preparation.class);
        final String newHeadStepId = preparation.getStep().id();

        head = repository.get(newHeadStepId, Step.class);
        headActions = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headActions.getActions(), hasSize(4));
        assertThat(headActions.getActions().get(0).getAction(), is("uppercase"));
        assertThat(headActions.getActions().get(1).getAction(), is("copy"));
        assertThat(headActions.getActions().get(2).getAction(), is("rename_column"));
        assertThat(headActions.getActions().get(3).getAction(), is("lowercase"));

        final Map<String, String> renameColumnFirstnameParameters = headActions.getActions().get(2).getParameters();
        assertThat(renameColumnFirstnameParameters.get("column_name"), is("firstname")); //check we have the rename firstname action
        assertThat(renameColumnFirstnameParameters.get("column_id"), is("0006")); //shifted id, was 0007

        final Map<String, String> copyColumnFirstnameParameters = headActions.getActions().get(1).getParameters();
        assertThat(copyColumnFirstnameParameters.get("column_name"), is("firstname")); //check we have the copy firstname action

        final Step renameCopyFirstnameStep = repository.get(head.getParent(), Step.class);
        final Step copyFirstnameStep = repository.get(renameCopyFirstnameStep.getParent(), Step.class);
        assertThat(copyFirstnameStep.getDiff().getCreatedColumns(), hasSize(1));
        assertThat(copyFirstnameStep.getDiff().getCreatedColumns(), hasItem("0006")); //shifted id, was 0007
    }

    @Test
    public void should_throw_exception_when_step_to_delete_is_root() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        applyTransformation(preparationId, "upper_case.json");
        applyTransformation(preparationId, "lower_case.json");

        // when: delete ROOT
        final Response response = when().delete("/preparations/{id}/actions/{action}", preparationId, ROOT_STEP.getId());

        //then
        response.then()
                .statusCode(403)
                .assertThat()
                .body("code", is("TDP_PS_PREPARATION_ROOT_STEP_CANNOT_BE_DELETED"));
    }

    @Test
    public void should_throw_exception_when_step_doesnt_exist() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        applyTransformation(preparationId, "upper_case.json");
        applyTransformation(preparationId, "lower_case.json");

        // when : delete unknown step
        final Response response = when().delete("/preparations/{id}/actions/{action}", preparationId, "azerty");

        //then
        response.then()
                .statusCode(400)
                .assertThat()
                .body("code", is("TDP_PS_PREPARATION_STEP_DOES_NOT_EXIST"));
    }

    @Test
    public void should_update_modification_date_on_delete_step() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        applyTransformation(preparationId, "upper_case.json");
        final String secondStepId = applyTransformation(preparationId, "lower_case.json");

        Preparation preparation = repository.get(preparationId, Preparation.class);
        final long oldModificationDate = preparation.getLastModificationDate();

        // when: delete LOWERCASE
        when().delete("/preparations/{id}/actions/{action}", preparation.id(), secondStepId);

        // then
        preparation = repository.get(preparation.id(), Preparation.class);
        assertThat(oldModificationDate, lessThan(preparation.getLastModificationDate()));
    }

    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------UTILS-------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    /**
     * Create a new preparation with the given name
     * @param name The preparation name
     * @return The preparation id
     */
    private String createPreparation(final String datasetId, final String name) {
        final Preparation preparation = new Preparation(datasetId, ROOT_STEP);
        preparation.setName(name);
        preparation.setCreationDate(0);
        repository.add(preparation);

        return preparation.id();
    }

    /**
     * Append an action to a preparation
     * @param preparationId The preparation id
     * @param transformationFilePath The transformation json file path
     * @return The created step id
     */
    private String applyTransformation(final String preparationId, final String transformationFilePath) throws IOException {
        given().body(IOUtils.toString(PreparationServiceTest.class.getResourceAsStream(transformationFilePath)))//
                .contentType(ContentType.JSON)//
                .when()//
                .post("/preparations/{id}/actions", preparationId)//
                .then()//
                .statusCode(200);

        final Preparation preparation = repository.get(preparationId, Preparation.class);
        return preparation.getStep().id();
    }

    /**
     * Assert that step has exactly the wanted created columns ids
     * @param stepId        The step id
     * @param columnsIds    The created columns ids
     */
    private void assertThatStepHasCreatedColumns(final String stepId, final String... columnsIds) {
        final Step head = repository.get(stepId, Step.class);
        assertThat(head.getDiff().getCreatedColumns(), hasSize(columnsIds.length));
        for(final String columnId : columnsIds) {
            assertThat(head.getDiff().getCreatedColumns(), hasItem(columnId));
        }
    }

    /**
     * Assert that the step is on the wanted column id
     * @param stepId    The step id
     * @param columnId  The column id
     */
    private void assertThatStepIsOnColumn(final String stepId, final String columnId) {
        final Step step = repository.get(stepId, Step.class);
        final PreparationActions stepActions = repository.get(step.getContent(), PreparationActions.class);
        final int stepActionIndex = stepActions.getActions().size() - 1;

        final Map<String, String> stepParameters = stepActions.getActions().get(stepActionIndex).getParameters();
        assertThat(stepParameters.get("column_id"), is(columnId));
    }
}
