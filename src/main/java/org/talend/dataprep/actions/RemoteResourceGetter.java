// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.actions;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOExceptionWithCause;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.service.Dictionaries;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A class that connects to a remote server based on a JWT-authentication and sends requests to retrieve data sets.
 */
public class RemoteResourceGetter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteResourceGetter.class);

    private final CloseableHttpClient client = HttpClients.custom().build();

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Login to the specified url with specified credentials to retrieve a JWT for following authentication.
     * 
     * @param url the url to connect to
     * @param userName the user name to use
     * @param passWord the password of the specified user
     * @return the authentication header containing the JWT token.
     */
    public Header login(String url, String userName, String passWord) {
        final URI uri;
        try {
            uri = new URI(url + "/login?client-app=STUDIO");
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("The provided url is not correct: " + url, e);
        }
        HttpUriRequest login = RequestBuilder.post().setUri(uri).addParameter("username", userName)
                .addParameter("password", passWord).build();

        try (CloseableHttpResponse response = client.execute(login)) {
            handleError(response);
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                if (StringUtils.equalsIgnoreCase("Authorization", header.getName())) {
                    return header;
                }
            }
        } catch (IOException e) {
            throw new RemoteConnectionException("Unable to log with the specified url", e);
        }
        throw new RemoteConnectionException(
                "The Authorization header is absent from the response when trying to connect to: " + url);
    }

    /**
     * Reads token of the specified JsonParser and returns a list of column metadata.
     *
     * @param jsonParser the jsonParser whose next tokens are supposed to represent a list of column metadata
     * @return The column metadata parsed from JSON parser.
     * @throws IOException In case of JSON exception related error.
     */
    private List<ColumnMetadata> parseAnArrayOfColumnMetadata(JsonParser jsonParser) throws IOException {
        try {
            List<ColumnMetadata> columns = new ArrayList<>();
            // skip the array beginning [
            jsonParser.nextToken();
            while (jsonParser.nextToken() != JsonToken.END_ARRAY && !jsonParser.isClosed()) {
                ColumnMetadata columnMetadata = jsonParser.readValueAs(ColumnMetadata.class);
                columns.add(columnMetadata);
            }
            if (columns.isEmpty()) {
                throw new IllegalArgumentException(
                        "No column metadata has been retrieved when trying to parse the retrieved data set.");
            }
            return columns;
        } catch (IOException e) {
            throw new IOExceptionWithCause("Unable to parse and retrieve the list of column metadata", e);
        }
    }

    private RowMetadata parseDataSetMetadataAndReturnRowMetadata(JsonParser jsonParser) throws IOException {
        try {
            RowMetadata rowMetadata = null;
            while (jsonParser.nextToken() != JsonToken.END_OBJECT && !jsonParser.isClosed()) {
                String currentField = jsonParser.getCurrentName();
                if (StringUtils.equalsIgnoreCase("columns", currentField)) {
                    rowMetadata = new RowMetadata(parseAnArrayOfColumnMetadata(jsonParser));
                }
            }
            LOGGER.debug("Skipping data to go back to the outer json object");
            while (jsonParser.getParsingContext().getParent().getCurrentName() != null && !jsonParser.isClosed()) {
                jsonParser.nextToken();
            }
            return rowMetadata;
        } catch (IOException e) {
            throw new IOExceptionWithCause("Unable to parse and retrieve the row metadata", e);
        }
    }

    private Map<String, DataSetRow> parseRecords(JsonParser jsonParser, RowMetadata rowMetadata, String joinOnColumn)
            throws IOException {
        try {
            Map<String, DataSetRow> lookupDataset = new HashMap<>();
            jsonParser.nextToken();
            while (jsonParser.nextToken() != JsonToken.END_ARRAY && !jsonParser.isClosed()) {
                Map<String, String> values = jsonParser.readValueAs(Map.class);
                DataSetRow row = new DataSetRow(rowMetadata, values);
                lookupDataset.put(row.get(joinOnColumn), row.clone());
            }
            if (lookupDataset.isEmpty()) {
                throw new IllegalArgumentException(
                        "No lookup record has been retrieved when trying to parse the retrieved data set.");
            }
            return lookupDataset;
        } catch (IOException e) {
            throw new IOExceptionWithCause("Unable to parse and retrieve the records of the data set", e);
        }
    }

    /**
     * Reads and Maps the data set from the specified input stream.
     *
     * @param inputStream the input stream containing the data set
     * @param joinOnColumn the column used to join the lookup data set
     * @return a map which associates to each value of the joint column its corresponding data set row
     * @throws IOException In case of JSON exception related error.
     */
    private Map<String, DataSetRow> parseAndMapLookupDataSet(InputStream inputStream, String joinOnColumn) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("The provided input stream must not be null");
        }

        try (JsonParser jsonParser = mapper.getFactory().createParser(inputStream)) {
            Map<String, DataSetRow> lookupDataset = new HashMap<>();
            RowMetadata rowMetadata = new RowMetadata();

            while (jsonParser.nextToken() != JsonToken.END_OBJECT && !jsonParser.isClosed()) {
                String currentField = jsonParser.getCurrentName();
                if (StringUtils.equalsIgnoreCase("metadata", currentField)) {
                    rowMetadata = parseDataSetMetadataAndReturnRowMetadata(jsonParser);
                }

                currentField = jsonParser.getCurrentName();
                if (StringUtils.equalsIgnoreCase("records", currentField)) {
                    lookupDataset = parseRecords(jsonParser, rowMetadata, joinOnColumn);
                }

            }
            if (lookupDataset.isEmpty()) {
                throw new RemoteConnectionException(
                        "No lookup data has been retrieved when trying to parse the specified data set .");
            }
            return lookupDataset;
        }
    }

    /**
     * Connects to the specified url with the specified JWT to retrieve the data set corresponding to the specified id and then
     * return
     * a map which associates to each value of the joint column its corresponding data set row.
     *
     * @param apiUrl the url to connect to
     * @param jwt the json web token
     * @param dataSetId the id of the data set to retrieve
     * @param joinOnColumn the column used to join the lookup data set
     * @return a map which associates to each value of the joint column its corresponding data set row
     */
    private Map<String, DataSetRow> mapLookupDataSet(String apiUrl, Header jwt, String dataSetId, String joinOnColumn) {
        String url = apiUrl + "/api/datasets/" + dataSetId + "?fullContent=true&includeTechnicalProperties=true";
        HttpGet request = new HttpGet(url);
        request.addHeader(jwt);
        try (final CloseableHttpResponse response = client.execute(request)) {
            handleError(response);
            return parseAndMapLookupDataSet(response.getEntity().getContent(), joinOnColumn);

        } catch (IOException e) {
            throw new RemoteConnectionException("Unable to retrieve the lookup dataset: " + dataSetId, e);
        }
    }

    /**
     * Login to the specified url with specified credentials and retrieves the data set corresponding to the specified id and then
     * return a map which associates to each value of the joint column its corresponding data set row.
     *
     * @param apiUrl the url to connect to
     * @param login the user name to use
     * @param password the password of the specified user
     * @param dataSetId the id of the data set to retrieve
     * @param joinOnColumn the column used to join the lookup data set
     * @return a map which associates to each value of the joint column its corresponding data set row
     */
    public Map<String, DataSetRow> retrieveLookupDataSet(String apiUrl, String login, String password, String dataSetId,
            String joinOnColumn) {
        Header jwt = login(apiUrl, login, password);
        return mapLookupDataSet(apiUrl, jwt, dataSetId, joinOnColumn);
    }

    public Dictionaries retrieveDictionaries(String apiUrl, String login, String password) {
        String url = apiUrl + "/api/transform/dictionary";
        HttpGet request = new HttpGet(url);
        request.addHeader(login(apiUrl, login, password));
        try (final CloseableHttpResponse response = client.execute(request)) {
            handleError(response);
            final ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(response.getEntity().getContent()));
            final Object object = ois.readObject();
            return (Dictionaries) object;
        } catch (Exception e) {
            throw new RemoteConnectionException("Unable to retrieve dictionaries.", e);
        }
    }

    public String retrievePreparation(String apiUrl, String login, String password, String preparationId) {
        final Header jwt = login(apiUrl, login, password);
        String url = apiUrl + "/api/preparations/" + preparationId + "/details";
        HttpGet request = new HttpGet(url);
        request.addHeader(jwt);
        try (final CloseableHttpResponse response = client.execute(request)) {
            handleError(response);
            return IOUtils.toString(response.getEntity().getContent());
        } catch (IOException e) {
            throw new RemoteConnectionException("Unable to retrieve the preparation with id: " + preparationId, e);
        }
    }

    /**
     * The exception thrown in case of connection exception
     */
    public static class RemoteConnectionException extends RuntimeException {

        public RemoteConnectionException(String message) {
            super(message);
        }

        public RemoteConnectionException(String message, Throwable e) {
            super(message, e);
        }

    }

    public void handleError(HttpResponse response) {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            try {
                DPErrorCode errorCode = mapper.readValue(response.getEntity().getContent(), DPErrorCode.class);
                throw new RemoteConnectionException("Status Code: " + statusCode + ", cause: " + errorCode.getCause()
                        + ", message: " + errorCode.getMessage() + ".");
            } catch (IOException e) {
                throw new RemoteConnectionException(
                        "Status Code: " + statusCode + ", cause: " + response.getStatusLine().getReasonPhrase() + ".", e);
            }
        }
    }
}