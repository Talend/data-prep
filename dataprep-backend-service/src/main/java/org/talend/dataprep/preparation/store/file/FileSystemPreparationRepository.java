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

package org.talend.dataprep.preparation.store.file;

import static org.apache.commons.lang.StringUtils.startsWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.preparation.Identifiable;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.preparation.store.ObjectPreparationRepository;
import org.talend.dataprep.preparation.store.PersistentStep;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.util.FilesHelper;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * File system implementation of preparation repository.
 */
@Component
@ConditionalOnProperty(name = "preparation.store", havingValue = "file")
public class FileSystemPreparationRepository extends ObjectPreparationRepository {

    /** This class' logger. */
    private static final Logger LOG = LoggerFactory.getLogger(FileSystemPreparationRepository.class);

    /** The dataprep ready jackson builder. */
    @Autowired
    private ObjectMapper mapper;

    /** Where to store the dataset metadata */
    @Value("${preparation.store.file.location}")
    private String preparationsLocation;

    /** Security to get the current user. */
    @Autowired
    private Security security;

    /**
     * Make sure the root folder is there.
     */
    @PostConstruct
    private void init() {
        getRootFolder().mkdirs();
        remove(Step.ROOT_STEP);
        final PersistentStep persistentStep = new PersistentStep();
        persistentStep.setId(Step.ROOT_STEP.id());
        remove(persistentStep);
        remove(PreparationActions.ROOT_ACTIONS);
    }

    /**
     * @see PreparationRepository#add(Identifiable)
     */
    @Override
    public void add(Identifiable object) {

        // defensive programming
        if (object == null) {
            LOG.warn("cannot save null...");
            return;
        }

        final File outputFile = getIdentifiableFile(object);
        try {
            FileUtils.touch(outputFile);
        } catch (IOException e) {
            LOG.error("Unable to prepare file for {}.", object, e);
        }

        try (GZIPOutputStream output = new GZIPOutputStream(new FileOutputStream(outputFile))) {
            mapper.writer().writeValue(output, object);
        } catch (IOException e) {
            LOG.error("Error saving {}", object, e);
            throw new TDPException(CommonErrorCodes.UNABLE_TO_SAVE_PREPARATION, e,
                    ExceptionContext.build().put("id", object.id()));
        }
        LOG.debug("{} #{} saved", object.getClass().getSimpleName(), object.id());
    }

    @Override
    public <T extends Identifiable> Stream<T> source(Class<T> clazz) {
        File[] files = getRootFolder().listFiles();
        if (files == null) {
            LOG.error("error listing preparations");
            files = new File[0];
        }
        final Stream<File> stream = Arrays.stream(files);
        return stream.filter(file -> startsWith(file.getName(), clazz.getSimpleName())) //
                .map(file -> read(file.getName(), clazz)) // read all files
                .filter(Objects::nonNull) // filter out null entries
                .filter(entry -> clazz.isAssignableFrom(entry.getClass())) // filter out the unwanted objects (should not be
                                                                           // necessary but you never know)
                .onClose(stream::close);
    }

    private <T extends Identifiable> T read(String id, Class<T> clazz) {

        final File from = getIdentifiableFile(clazz, id);
        if (from.getName().startsWith(".")) {
            LOG.info("Ignore hidden file {}", from.getName());
            return null;
        }
        if (!from.exists()) {
            LOG.debug("{} #{} not found in file system", clazz.getSimpleName(), id);
            return null;
        }

        T result;
        try (GZIPInputStream input = new GZIPInputStream(new FileInputStream(from))) {
            result = mapper.readerFor(clazz).readValue(input);
        } catch (IOException e) {
            LOG.error("error reading preparation file {}", from.getAbsolutePath(), e);
            return null;
        }

        return result;
    }

    /**
     * @see PreparationRepository#clear()
     */
    @Override
    public void clear() {

        // clear all files
        final File[] preparations = getRootFolder().listFiles();
        if (preparations != null) {
            for (File file : preparations) {
                FilesHelper.deleteQuietly(file);
            }
        }

        LOG.debug("preparation repository cleared");
    }

    /**
     * @see PreparationRepository#remove(Identifiable)
     */
    @Override
    public void remove(Identifiable object) {
        if (object == null) {
            return;
        }
        final File file = getIdentifiableFile(object);
        FilesHelper.deleteQuietly(file);
        LOG.debug("identifiable {} #{} removed", object.getClass().getSimpleName(), object.id());
    }

    private File getIdentifiableFile(Identifiable object) {
        return getIdentifiableFile(object.getClass(), object.id());
    }

    /**
     * Return the file that matches the given identifiable id.
     *
     * @param clazz the identifiable class.
     * @param id the identifiable... id !
     * @return the file where to read/write the identifiable object.
     */
    private File getIdentifiableFile(Class clazz, String id) {
        return new File(preparationsLocation + '/' + clazz.getSimpleName() + '-' + stripOptionalPrefix(clazz, id));
    }

    /**
     * Remove the optional classname prefix if the given id already has it.
     *
     * For instance : "Preparation-a99a05a862c6a220d7977f97cd9cb3f71d640592" returns
     * "a99a05a862c6a220d7977f97cd9cb3f71d640592" "a99a05a862c6a220d7977f97cd9cb3f71d640592" returns
     * "a99a05a862c6a220d7977f97cd9cb3f71d640592"
     *
     * @param clazz the class of the wanted object.
     * @param id the object id.
     * @return the id striped of the classname prefix if needed.
     */
    private String stripOptionalPrefix(Class clazz, String id) {

        if (StringUtils.isBlank(id)) {
            return null;
        }

        final String className = clazz.getSimpleName();
        if (id.startsWith(className)) {
            return id.substring(className.length() + 1);
        }
        return id;
    }
    /**
     * Return the root folder where the preparations are stored.
     *
     * @return the root folder.
     */
    private File getRootFolder() {
        return new File(preparationsLocation);
    }
}
