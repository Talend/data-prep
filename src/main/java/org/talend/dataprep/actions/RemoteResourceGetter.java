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
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.GZIPInputStream;

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
import org.talend.dataprep.api.dataset.DataSetDataReader;
import org.talend.dataprep.api.dataset.row.LightweightExportableDataSet;
import org.talend.dataprep.transformation.service.Dictionaries;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A class that connects to a remote server based on a JWT-authentication and sends requests to retrieve data sets.
 */
public class RemoteResourceGetter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteResourceGetter.class);

    private final CloseableHttpClient client = HttpClients.custom().build();

    private final ObjectMapper mapper = new ObjectMapper();

    private final DataSetDataReader dataSetDataReader = new DataSetDataReader(mapper);

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
    private LightweightExportableDataSet mapLookupDataSet(String apiUrl, Header jwt, String dataSetId, String joinOnColumn) {
        String url = apiUrl + "/api/datasets/" + dataSetId + "?fullContent=true&includeTechnicalProperties=true";
        HttpGet request = new HttpGet(url);
        request.addHeader(jwt);
        try (final CloseableHttpResponse response = client.execute(request)) {
            handleError(response);
            return dataSetDataReader.parseAndMapLookupDataSet(response.getEntity().getContent(), joinOnColumn);
        } catch (IllegalArgumentException | IOException e) {
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
    public LightweightExportableDataSet retrieveLookupDataSet(String apiUrl, String login, String password, String dataSetId,
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

    String retrievePreparation(String apiUrl, String login, String password, String preparationId) {
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

        RemoteConnectionException(String message) {
            super(message);
        }

        RemoteConnectionException(String message, Throwable e) {
            super(message, e);
        }

    }

    void handleError(HttpResponse response) {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            try {
                DPErrorCode errorCode = mapper.readValue(response.getEntity().getContent(), DPErrorCode.class);
                throw new RemoteConnectionException("Status Code: " + statusCode + ", cause: " + errorCode.getCause()
                        + ", message: " + errorCode.getMessage() + ".");
            } catch (IOException e) {
                LOGGER.debug("Communication error with server.", e);
                throw new RemoteConnectionException(
                        "Status Code: " + statusCode + ", cause: " + response.getStatusLine().getReasonPhrase() + ".");
            }
        }
    }
}
