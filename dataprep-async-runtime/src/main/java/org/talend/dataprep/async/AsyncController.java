// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.async;

import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.async.AsyncController.QUEUE_PATH;

import java.util.concurrent.CancellationException;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.async.repository.ManagedTaskRepository;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;

@RestController
@RequestMapping("/" + QUEUE_PATH)
public class AsyncController {

    static final String QUEUE_PATH = "queue";

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncController.class);

    @Autowired
    private ManagedTaskExecutor executor;

    @Autowired
    private ManagedTaskRepository repository;

    @GetMapping(path = "/{id}")
    public AsyncExecution get(@PathVariable("id") String id) {
        LOGGER.debug("Get execution {}", id);
        return repository.get(id);
    }

    @GetMapping
    public Stream<AsyncExecution> list() {
        return repository.list();
    }

    @DeleteMapping(path = "/{id}")
    public AsyncExecution cancel(@PathVariable("id") String id) {
        LOGGER.debug("Cancel execution {}", id);
        try {
            return executor.cancel(id);
        } catch (CancellationException e) {
            LOGGER.debug("Cannot cancel exception.", e);
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_CANCEL_EXECUTION, build().put("id", id));
        }
    }

}
