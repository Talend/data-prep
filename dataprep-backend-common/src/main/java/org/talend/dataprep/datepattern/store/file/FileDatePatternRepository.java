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
package org.talend.dataprep.datepattern.store.file;

import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.exception.error.DatepatternErrorCodes.*;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.talend.dataprep.datepattern.DatePattern;
import org.talend.dataprep.datepattern.DatePatternRepository;
import org.talend.dataprep.exception.TDPException;

@Component("datePatternRepository#file")
@ConditionalOnProperty(name = "datepattern.store", havingValue = "file")
public class FileDatePatternRepository implements DatePatternRepository {

    /**
     * Where to store the datepattern.
     * file name is the pattern so we store a file per pattern (not concurrent access to a file to manage)
     */
    @Value( "${datepattern.store.file.location}" )
    private String datepatternLocation;

    /**
     * Make sure the root folder is there.
     */
    @PostConstruct
    private void init() {
        try {
            Path rootPath = getRootFolder();
            if (!Files.exists(rootPath)) {
                Files.createDirectories(rootPath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Return the root folder where the datepattern are stored.
     *
     * @return the root folder.
     */
    private Path getRootFolder() {
        return Paths.get(datepatternLocation);
    }

    @Override
    public void add(DatePattern datePattern) {
        try {
            Files.createFile(Paths.get(getRootFolder().toString(), datePattern.getPattern()));
        } catch (FileAlreadyExistsException e) {
            // we can ignore this one
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_ADD_DATEPATTERN, e, build().put("pattern", datePattern.getPattern()));
        }
    }

    @Override
    public List<DatePattern> all() {
        List<DatePattern> all = new ArrayList<>();
        try {
            Files.list(getRootFolder()).forEach(path -> {
                all.add(new DatePattern(path.toFile().getName()));
            });
            return all;
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_LIST_DATEPATTERN, e, build());
        }
    }

    @Override
    public void remove(DatePattern datePattern) {
        try {
            Files.deleteIfExists(Paths.get(getRootFolder().toString(), datePattern.getPattern()));
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_REMOVE_DATEPATTERN, e, build().put("pattern", datePattern.getPattern()));
        }
    }

    @Override
    public void clear() {
        try {
            Files.delete(getRootFolder());
            init();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }
}
