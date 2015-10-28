package org.talend.dataprep.transformation.service;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.http.HttpStatus.OK;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.springframework.test.util.AssertionErrors;
import org.talend.dataprep.transformation.Application;
import uk.co.datumedge.hamcrest.json.SameJSONAs;

/**
 * Integration tests on actions.
 */
public class TransformTests extends TransformationServiceBaseTests {

    @Test
    public void emptyTransformation() {
        given()//
                .multiPart("content", "")//
                .when()//
                .post("/transform/JSON")//
                .then()//
                .statusCode(OK.value());
    }

    @Test
    public void noAction() throws Exception {
        // given
        final String initialContent = IOUtils.toString(Application.class.getResourceAsStream("actions/input_case.json"));

        // when
        final String transformedContent = given()//
                .multiPart("actions", "")//
                .multiPart("content", initialContent)//
                .when().post("/transform/JSON")//
                .asString();

        // then
        assertEquals(initialContent, transformedContent, false);
    }

    @Test
    public void noActionWithCarrierReturn() throws Exception {
        // given
        final String initialContent = IOUtils
                .toString(Application.class.getResourceAsStream("actions/input_with_carrier_return.json"));

        // when
        final String transformedContent = given()//
                .multiPart("actions", "")//
                .multiPart("content", initialContent)//
                .when()//
                .post("/transform/JSON")//
                .asString();

        // then
        assertEquals(initialContent, transformedContent, false);
    }

    @Test
    public void testInvalidJSONInput() throws Exception {
        given()//
                .multiPart("content", "invalid content on purpose.")//
                .when()//
                .post("/transform/JSON")//
                .then()//
                .statusCode(400)//
                .content("code", is("TDP_ALL_UNABLE_TO_PARSE_JSON"));
    }

    // ------------------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------Actions---------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------------

    @Test
    public void uppercaseAction() throws Exception {
        // given
        final String actions = IOUtils.toString(Application.class.getResourceAsStream("actions/uppercaseAction.json"));
        final String initialContent = IOUtils.toString(Application.class.getResourceAsStream("actions/input_case.json"));
        final String expectedContent = IOUtils
                .toString(Application.class.getResourceAsStream("actions/uppercaseAction_expected.json"));

        // when
        final String transformedContent = given() //
                .multiPart("actions", actions) //
                .multiPart("content", initialContent) //
                .when() //
                .post("/transform/JSON") //
                .asString();

        // then
        assertEquals(expectedContent, transformedContent, false);
    }

    @Test
    public void lowercaseAction() throws Exception {
        // given
        final String actions = IOUtils.toString(Application.class.getResourceAsStream("actions/lowercaseAction.json"));
        final String initialContent = IOUtils.toString(Application.class.getResourceAsStream("actions/input_case.json"));
        final String expectedContent = IOUtils
                .toString(Application.class.getResourceAsStream("actions/lowercaseAction_expected.json"));

        // when
        final String transformedContent = given() //
                .multiPart("actions", actions) //
                .multiPart("content", initialContent) //
                .when() //
                .post("/transform/JSON") //
                .asString();

        // then
        assertEquals(expectedContent, transformedContent, false);
    }

    @Test
    public void lowercaseActionWithFilter() throws Exception {
        // given
        final String actions = IOUtils.toString(Application.class.getResourceAsStream("actions/lowercaseAction_filter.json"));
        final String initialContent = IOUtils.toString(Application.class.getResourceAsStream("actions/input_case.json"));
        final String expectedContent = IOUtils
                .toString(Application.class.getResourceAsStream("actions/lowercaseAction_filter_expected.json"));

        // when
        final String transformedContent = given() //
                .multiPart("actions", actions) //
                .multiPart("content", initialContent) //
                .when() //
                .post("/transform/JSON") //
                .asString();

        // then
        assertEquals(expectedContent, transformedContent, false);
    }

    @Test
    public void splitActionWithFilter() throws Exception {
        // given
        final String actions = IOUtils.toString(Application.class.getResourceAsStream("actions/splitAction_filter.json"));
        final String initialContent = IOUtils.toString(Application.class.getResourceAsStream("actions/input_changeDomainAction.json"));
        final String expectedContent = IOUtils
                .toString(Application.class.getResourceAsStream("actions/splitAction_filter_expected.json"));

        // when
        final String transformedContent = given() //
                .multiPart("actions", actions) //
                .multiPart("content", initialContent) //
                .when() //
                .post("/transform/JSON") //
                .asString();

        // then
        assertEquals(expectedContent, transformedContent, false);
    }


    @Test
    public void fillEmptyWithDefaultAction() throws Exception {
        // given
        final String actions = IOUtils.toString(Application.class.getResourceAsStream("actions/fillEmptyWithDefaultAction.json"));
        final String initialContent = IOUtils.toString(Application.class.getResourceAsStream("actions/input.json"));
        final String expectedContent = IOUtils
                .toString(Application.class.getResourceAsStream("actions/fillEmptyWithDefaultAction_expected.json"));

        // when
        final String transformedContent = given() //
                .multiPart("actions", actions) //
                .multiPart("content", initialContent) //
                .when() //
                .post("/transform/JSON") //
                .asString();

        // then
        assertEquals(expectedContent, transformedContent, false);
    }

    @Test
    public void fillEmptyWithDefaultActionBoolean() throws Exception {
        // given
        final String actions = IOUtils
                .toString(Application.class.getResourceAsStream("actions/fillEmptyWithDefaultBooleanAction.json"));
        final String initialContent = IOUtils.toString(Application.class.getResourceAsStream("actions/input.json"));
        final String expectedContent = IOUtils
                .toString(Application.class.getResourceAsStream("actions/fillEmptyWithDefaultBooleanAction_expected.json"));

        // when
        final String transformedContent = given() //
                .multiPart("actions", actions) //
                .multiPart("content", initialContent) //
                .when() //
                .post("/transform/JSON") //
                .asString();

        // then
        assertEquals(expectedContent, transformedContent, false);
    }

    @Test
    public void fillEmptyWithDefaultActionInteger() throws Exception {
        // given
        final String actions = IOUtils
                .toString(Application.class.getResourceAsStream("actions/fillEmptyWithDefaultIntegerAction.json"));
        final String initialContent = IOUtils.toString(Application.class.getResourceAsStream("actions/input.json"));
        final String expectedContent = IOUtils
                .toString(Application.class.getResourceAsStream("actions/fillEmptyWithDefaultIntegerAction_expected.json"));

        // when
        final String transformedContent = given() //
                .multiPart("actions", actions) //
                .multiPart("content", initialContent) //
                .when() //
                .post("/transform/JSON") //
                .asString();

        // then
        assertEquals(expectedContent, transformedContent, false);
    }

    @Test
    public void negateActionBoolean() throws Exception {
        // given
        final String actions = IOUtils.toString(Application.class.getResourceAsStream("actions/negateAction.json"));
        final String initialContent = IOUtils.toString(Application.class.getResourceAsStream("actions/input.json"));
        final String expectedContent = IOUtils
                .toString(Application.class.getResourceAsStream("actions/negateAction_expected.json"));

        // when
        final String transformedContent = given() //
                .multiPart("actions", actions) //
                .multiPart("content", initialContent) //
                .when() //
                .post("/transform/JSON") //
                .asString();

        // then
        assertEquals(expectedContent, transformedContent, false);
    }

    @Test
    public void cutAction() throws Exception {
        // given
        final String actions = IOUtils.toString(Application.class.getResourceAsStream("actions/cutAction.json"));
        final String initialContent = IOUtils.toString(Application.class.getResourceAsStream("actions/input_cut.json"));
        final String expectedContent = IOUtils.toString(Application.class.getResourceAsStream("actions/cutAction_expected.json"));

        // when
        final String transformedContent = given() //
                .multiPart("actions", actions) //
                .multiPart("content", initialContent) //
                .when() //
                .post("/transform/JSON") //
                .asString();

        // then
        assertEquals(expectedContent, transformedContent, false);
    }

    @Test
    public void duplicateAction() throws Exception {
        // given
        final String actions = IOUtils.toString(Application.class.getResourceAsStream("actions/duplicateAction.json"));
        final String initialContent = IOUtils.toString(Application.class.getResourceAsStream("actions/input_duplicate.json"));
        final String expectedContent = IOUtils
                .toString(Application.class.getResourceAsStream("actions/duplicateAction_expected.json"));

        // when
        final String transformedContent = given() //
                .multiPart("actions", actions) //
                .multiPart("content", initialContent) //
                .when() //
                .post("/transform/JSON") //
                .asString();

        // then
        assertEquals(expectedContent, transformedContent, false);
    }

    @Test
    public void deleteEmptyActionString() throws Exception {
        // given
        final String actions = IOUtils.toString(Application.class.getResourceAsStream("actions/deleteEmptyAction.json"));
        final String initialContent = IOUtils.toString(Application.class.getResourceAsStream("actions/input.json"));
        final String expectedContent = IOUtils
                .toString(Application.class.getResourceAsStream("actions/deleteEmptyAction_expected.json"));

        // when
        final String transformedContent = given() //
                .multiPart("actions", actions) //
                .multiPart("content", initialContent) //
                .when() //
                .post("/transform/JSON") //
                .asString();

        // then
        assertEquals(expectedContent, transformedContent, false);
    }

    @Test
    public void absoluteIntAction() throws Exception {
        // given
        final String actions = IOUtils.toString(Application.class.getResourceAsStream("actions/absoluteIntAction.json"));
        final String initialContent = IOUtils
                .toString(Application.class.getResourceAsStream("actions/input_absoluteAction.json"));
        final String expectedContent = IOUtils
                .toString(Application.class.getResourceAsStream("actions/absoluteIntAction_expected.json"));

        // when
        final String transformedContent = given() //
                .multiPart("actions", actions) //
                .multiPart("content", initialContent) //
                .when() //
                .post("/transform/JSON") //
                .asString();

        // then
        assertEquals(expectedContent, transformedContent, false);
    }

    @Test
    public void absoluteFloatAction() throws Exception {
        // given
        final String actions = IOUtils.toString(Application.class.getResourceAsStream("actions/absoluteFloatAction.json"));
        final String initialContent = IOUtils
                .toString(Application.class.getResourceAsStream("actions/input_absoluteAction.json"));
        final String expectedContent = IOUtils
                .toString(Application.class.getResourceAsStream("actions/absoluteFloatAction_expected.json"));

        // when
        final String transformedContent = given() //
                .multiPart("actions", actions) //
                .multiPart("content", initialContent) //
                .when() //
                .post("/transform/JSON") //
                .asString();

        // then
        assertEquals(expectedContent, transformedContent, false);
    }

    @Test
    public void replaceOnValueAction() throws Exception {
        // given
        final String actions = IOUtils.toString(Application.class.getResourceAsStream("actions/replaceOnValueAction.json"));
        final String initialContent = IOUtils.toString(Application.class.getResourceAsStream("actions/input.json"));
        final String expectedContent = IOUtils
                .toString(Application.class.getResourceAsStream("actions/replaceOnValueAction_expected.json"));

        // when
        final String transformedContent = given() //
                .multiPart("actions", actions) //
                .multiPart("content", initialContent) //
                .when() //
                .post("/transform/JSON") //
                .asString();

        // then
        assertEquals(expectedContent, transformedContent, false);
    }

    @Test
    public void transformStateColumn() throws Exception {
        // given
        final String actions = IOUtils.toString(Application.class.getResourceAsStream("actions/lowercaseAction_state.json"));
        final String initialContent = IOUtils.toString(Application.class.getResourceAsStream("actions/input_state.json"));
        final String expectedContent = IOUtils
                .toString(Application.class.getResourceAsStream("actions/lowercaseAction_state_expected.json"));

        // when
        final String transformedContent = given() //
                .multiPart("actions", actions) //
                .multiPart("content", initialContent) //
                .when() //
                .post("/transform/JSON") //
                .asString();

        // then
        assertEquals(expectedContent, transformedContent, false);
    }

    @Test
    public void domainChangeAction() throws Exception {
        // given
        final String actions = IOUtils.toString(Application.class.getResourceAsStream("actions/changeDomainAction.json"));
        final String initialContent = IOUtils.toString(Application.class.getResourceAsStream("actions/input_changeDomainAction.json"));
        final String expectedContent = IOUtils
                .toString(Application.class.getResourceAsStream("actions/changeDomainAction_expected.json"));

        // when
        final String transformedContent = given() //
                .multiPart("actions", actions) //
                .multiPart("content", initialContent) //
                .when() //
                .post("/transform/JSON") //
                .asString();

        // then
        assertThat(transformedContent, sameJSONAs(expectedContent).allowingExtraUnexpectedFields());
    }
}
