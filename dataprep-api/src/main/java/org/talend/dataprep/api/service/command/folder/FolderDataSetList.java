package org.talend.dataprep.api.service.command.folder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderContent;
import org.talend.dataprep.api.service.PreparationAPI;
import org.talend.dataprep.api.service.command.ReleasableInputStream;
import org.talend.dataprep.api.service.command.common.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.HystrixCommand;
import org.talend.dataprep.exception.error.DataSetErrorCodes;

@Component
@Scope("request")
public class FolderDataSetList extends GenericCommand<FolderContent> {

    private FolderDataSetList(HttpClient client, String sort, String order) {
        this(client, sort, order, null);
    }

    private FolderDataSetList(HttpClient client, String sort, String order, String folder) {
        super(PreparationAPI.TRANSFORM_GROUP, client);

        try {
            execute(() -> onExecute(sort, order, folder));
            onError(e -> new TDPException(APIErrorCodes.UNABLE_TO_LIST_DATASETS, e));
            on(HttpStatus.NO_CONTENT, HttpStatus.ACCEPTED).then(emptyFolderContent());
            on(HttpStatus.OK).then(buildContent(folder, order, sort));

        } catch (Exception e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    private HttpRequestBase onExecute(String sort, String order, String folder) {
        try {

            URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl + "/datasets");
            uriBuilder.addParameter("sort", sort);
            uriBuilder.addParameter("order", order);
            if (StringUtils.isNotEmpty(folder)) {
                uriBuilder.addParameter("folder", folder);
            }
            return new HttpGet(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    public BiFunction<HttpRequestBase, HttpResponse, FolderContent> emptyFolderContent() {
        return (request, response) -> {
            request.releaseConnection();
            return new FolderContent();
        };
    }

    public BiFunction<HttpRequestBase, HttpResponse, FolderContent> buildContent(final String searchFolder, final String order, final String sort) {
        return (request, response) -> {
            try {
                ObjectMapper mapper = builder.build();
                List<DataSetMetadata> datasets = mapper.readValue(
                        new ReleasableInputStream(response.getEntity().getContent(), request::releaseConnection), //
                        new TypeReference<List<DataSetMetadata>>() {
                });

                // now get folders
                final HystrixCommand<InputStream> foldersList = context.getBean(FoldersList.class, client, searchFolder);

                List<Folder> folders = mapper.readValue(
                        new ReleasableInputStream(foldersList.execute(), request::releaseConnection), //
                        new TypeReference<List<Folder>>() {
                });


                final Comparator<String> comparisonOrder;
                switch (order.toUpperCase()) {
                    case "ASC":
                        comparisonOrder = Comparator.naturalOrder();
                        break;
                    case "DESC":
                        comparisonOrder = Comparator.reverseOrder();
                        break;
                    default:
                        throw new TDPException( DataSetErrorCodes.ILLEGAL_ORDER_FOR_LIST, ExceptionContext.build().put( "order", order));
                }
                // Select comparator for sort (either by name or date)
                final Comparator<Folder> comparator;
                switch (sort.toUpperCase()) {
                    case "NAME":
                        comparator = Comparator.comparing(folder -> folder.getName().toUpperCase(), comparisonOrder);
                        break;
                    case "DATE":
                        comparator = Comparator.comparing(folder -> String.valueOf(folder.getCreationDate()), comparisonOrder);
                        break;
                    default:
                        throw new TDPException(DataSetErrorCodes.ILLEGAL_SORT_FOR_LIST, ExceptionContext.build().put("sort", order));
                }

                folders.sort( comparator );

                return new FolderContent(datasets, folders);
            } catch (IOException e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        };
    }

}
