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

package org.talend.dataprep.preparation.store.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.preparation.Identifiable;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.util.FilesHelper;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * File system implementation of preparation repository.
 */
@Component
@ConditionalOnProperty(name = "preparation.store", havingValue = "file")
public class FileSystemPreparationRepository implements PreparationRepository {

    /** This class' logger. */
    private static final Logger LOG = LoggerFactory.getLogger(FileSystemPreparationRepository.class);

    /** The dataprep ready jackson builder. */
    @Autowired
    private ObjectMapper mapper;

    /** The root step. */
    @Resource(name = "rootStep")
    private Step rootStep;

    /** The default root content. */
    @Resource(name = "rootContent")
    private PreparationActions rootContent;

    /** Where to store the dataset metadata */
    @Value("${preparation.store.file.location}")
    private String preparationsLocation;

    /**
     * Make sure the root folder is there.
     */
    @PostConstruct
    private void init() {
        getRootFolder().mkdirs();
        add(rootContent);
        add(rootStep);
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

        try (GZIPOutputStream output = new GZIPOutputStream(new FileOutputStream(outputFile))) {
            mapper.writer().writeValue(output, object);
        } catch (IOException e) {
            LOG.error("Error saving {}", object, e);
            throw new TDPException(CommonErrorCodes.UNABLE_TO_SAVE_PREPARATION, e,
                    ExceptionContext.build().put("id", object.id()));
        }
        LOG.debug("preparation #{} saved", object.id());
    }

    /**
     * @see PreparationRepository#get(String, Class)
     */
    @Override
    public <T extends Identifiable> T get(String id, Class<T> clazz) {

        final File from = getIdentifiableFile(clazz, id);
        if (from.getName().startsWith(".")) {
            LOG.info("Ignore hidden file {}", from.getName());
            return null;
        }
        if (!from.exists()) {
            LOG.debug("preparation #{} not found in file system", id);
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
     * @see PreparationRepository#getByDataSet(String)
     */
    @Override
    public Collection<Preparation> getByDataSet(String dataSetId) {

        // defensive programming
        if (StringUtils.isEmpty(dataSetId)) {
            return Collections.emptyList();
        }

        // first filter on the class (listAll()) and then second filter on the dataset id
        return listAll(Preparation.class) //
                .stream() //
                .filter(preparation -> dataSetId.equals(preparation.getDataSetId())) //
                .collect(Collectors.toList());
    }

    /**
     * @see PreparationRepository#listAll(Class)
     */
    @Override
    public <T extends Identifiable> Collection<T> listAll(Class<T> clazz) {
        //@formatter:off
        File[] files = getRootFolder().listFiles();
        if(files == null) {
            LOG.error("error listing preparations");
            files = new File[0];
        }
        Collection<T> result;
        try (final Stream<File> stream = Arrays.stream(files)) {
             result = stream.filter(file -> StringUtils.startsWith(file.getName(), clazz.getSimpleName()))
                    .map(file -> get(file.getName(), clazz))                   // read all files
                    .filter(entry -> entry != null)                            // filter out null entries
                    .filter(entry -> clazz.isAssignableFrom(entry.getClass())) // filter out the unwanted objects (should not be necessary but you never know)
                    .collect(Collectors.toSet());                              // and put it in a set
        }
        //@formatter:on
        LOG.debug("There are {} for class {}", result.size(), clazz.getName());
        return result;
    }

    /**
     * @see PreparationRepository#clear()
     */
    @Override
    public void clear() {

        // clear all files
        final File[] preparations = getRootFolder().listFiles();
        for (File file : preparations) {
            FilesHelper.deleteQuietly(file);
        }

        // add the default files
        add(rootContent);
        add(rootStep);

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
        LOG.debug("preparation #{} removed", object.id());
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
