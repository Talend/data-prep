// ============================================================================
//
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

package org.talend.dataprep.schema.xls;

import java.io.*;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.schema.Serializer;
import org.talend.dataprep.schema.xls.serialization.XlsRunnable;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.talend.dataprep.schema.xls.serialization.XlsxStreamRunnable;

@Service("serializer#xls")
public class XlsSerializer implements Serializer {

    /** Dataprep ready to use jackson object mapper. */
    @Autowired
    private ObjectMapper mapper;

    /** Task executor to serialize in an asynchronously. */
    @Resource(name = "serializer#excel#executor")
    private TaskExecutor executor;


    /**
     * @see Serializer#serialize(InputStream, DataSetMetadata)
     */
    @Override
    public InputStream serialize(InputStream givenInputStream, DataSetMetadata metadata) {
        try {

            PipedInputStream pipe = new PipedInputStream();
            PipedOutputStream jsonOutput = new PipedOutputStream(pipe);

            // override the parameter in case it needs to be wrapped in a buffered inputstream
            InputStream inputStream = givenInputStream;
            if (!inputStream.markSupported()) {
                inputStream = new BufferedInputStream(inputStream);
            }

            inputStream.mark(Integer.MAX_VALUE);

            boolean newExcelFormat = XlsUtils.isNewExcelFormat(inputStream);

            inputStream.reset();

            Runnable runnable = newExcelFormat ? //
                serializeNew(inputStream, metadata, jsonOutput) : serializeOld(inputStream, metadata, jsonOutput);

            // Serialize asynchronously for better performance (especially if caller doesn't consume all, see sampling).
            executor.execute(runnable);

            return pipe;
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_SERIALIZE_TO_JSON, e);
        }
    }

    public static boolean isHeaderLine(int lineIndex, List<ColumnMetadata> columns) {
        boolean headerLine = false;
        for (ColumnMetadata columnMetadata : columns) {
            if (lineIndex < columnMetadata.getHeaderSize()) {
                headerLine = true;
            }
        }
        return headerLine;
    }


    private Runnable serializeNew(InputStream rawContent, DataSetMetadata metadata, PipedOutputStream jsonOutput) {
        return new XlsxStreamRunnable( jsonOutput, rawContent, metadata, mapper.getFactory());
    }

    private Runnable serializeOld(InputStream rawContent, DataSetMetadata metadata, PipedOutputStream jsonOutput)
        throws IOException {
        return new XlsRunnable(rawContent, jsonOutput, metadata,  mapper.getFactory());
    }



}