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

package org.talend.dataprep.util;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.exception.error.CommonErrorCodes.ILLEGAL_ORDER_FOR_LIST;
import static org.talend.dataprep.exception.error.CommonErrorCodes.ILLEGAL_SORT_FOR_LIST;

import java.beans.PropertyEditor;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.dataset.service.UserDataSetMetadata;
import org.talend.dataprep.exception.TDPException;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;

/**
 * Utility class used to sort and order DataSets or Preparations.
 */
public final class SortAndOrderHelper {

    /** Order to apply to a sort. */
    public enum Order {
        /** Ascending order. */
        ASC,
        /** Descending order. */
        DESC;

        public String camelName() {
            return snakeToCamelCaseConverter.convert(name());
        }
    }

    /**
     * How to sort things.
     * Might be a good idea to replace by {@link org.springframework.data.domain.Sort}
     */
    public enum Sort {
        /** Name of the entity. */
        NAME,
        /** Creator of the entity. */
        AUTHOR,
        /** @deprecated use {@link #CREATION_DATE} or {@link #LAST_MODIFICATION_DATE}. */
        @Deprecated
        DATE,
        /** Creation date. */
        CREATION_DATE,
        /** Last modification date. {@link Preparation#lastModificationDate} */
        LAST_MODIFICATION_DATE,
        /**
         * Number of steps of a preparation.
         */
        NB_STEPS,
        /**
         * Number of records of a data set: {@link org.talend.dataprep.api.dataset.DataSetContent#nbRecords}.
         */
        NB_RECORDS,
        /**
         * Name of the dataset referred by this entity.
         * When listing preparations, allows sort on dataset name.
         */
        DATASET_NAME;

        public String camelName() {
            return snakeToCamelCaseConverter.convert(name());
        }
    }

    private static final Logger LOGGER = getLogger(SortAndOrderHelper.class);

    private static final Converter<String, String> snakeToCamelCaseConverter = CaseFormat.UPPER_UNDERSCORE
            .converterTo(CaseFormat.LOWER_CAMEL);

    private SortAndOrderHelper() {
    }

    /**
     * Create a {@link PropertyEditor} to allow binding of lower-case {@link Order} in
     * {@link org.springframework.web.bind.annotation.RequestParam @RequestParam}.
     */
    public static PropertyEditor getOrderPropertyEditor() {
        return new ConverterBasedPropertyEditor<>(Order::valueOf);
    }

    /**
     * Create a {@link PropertyEditor} to allow binding of lower-case {@link Sort} in
     * {@link org.springframework.web.bind.annotation.RequestParam @RequestParam}.
     */
    public static PropertyEditor getSortPropertyEditor() {
        return new ConverterBasedPropertyEditor<>(Sort::valueOf);
    }

    /**
     * Return the string comparator to use for the given order name.
     *
     * @param orderKey the name of the order to apply. If null, default to {@link Order#ASC}.
     * @return the string comparator to use for the given order name.
     */
    private static Comparator<Comparable> getOrderComparator(Order orderKey) {
        final Comparator<Comparable> comparisonOrder;
        if (orderKey == null) {
            comparisonOrder = Comparator.naturalOrder();
        } else {
            switch (orderKey) {
            case ASC:
                comparisonOrder = Comparator.naturalOrder();
                break;
            case DESC:
                comparisonOrder = Comparator.reverseOrder();
                break;
            default:
                // this should not be possible
                throw new TDPException(ILLEGAL_ORDER_FOR_LIST, build().put("order", orderKey));
            }
        }
        return comparisonOrder;
    }

    /**
     * Return a dataset metadata comparator from the given parameters.
     *
     * @param sortKey the sort key. If null, default to {@link Sort#NAME}.
     * @param orderKey the order key to use. If null, default to {@link Order#ASC}.
     * @return a dataset metadata comparator from the given parameters.
     */
    public static Comparator<DataSetMetadata> getDataSetMetadataComparator(Sort sortKey, Order orderKey) {
        Comparator<Comparable> comparisonOrder = getOrderComparator(orderKey);

        // Select comparator for sort (either by name or date)
        Function<DataSetMetadata, Comparable> keyExtractor;
        if (sortKey == null) { // default to NAME sort
            keyExtractor = SortAndOrderHelper::extractDataSetName;
        } else {
            switch (sortKey) {
            // In case of API call error, default to NAME sort
            case DATASET_NAME:
            case NB_STEPS:
            case NAME:
                keyExtractor = SortAndOrderHelper::extractDataSetName;
                break;
            case AUTHOR:
                keyExtractor = dataSetMetadata -> Optional.ofNullable(dataSetMetadata)
                        .filter(ds -> ds instanceof UserDataSetMetadata)
                        .filter(ds -> ((UserDataSetMetadata) ds).getOwner() != null)
                        .filter(ds -> ((UserDataSetMetadata) ds).getOwner().getDisplayName() != null)
                        .map(ds -> ((UserDataSetMetadata) ds).getOwner().getDisplayName().toUpperCase())
                        .orElse(EMPTY);
                break;
            case CREATION_DATE:
            case DATE:
                keyExtractor = DataSetMetadata::getCreationDate;
                break;
            case LAST_MODIFICATION_DATE:
                keyExtractor = DataSetMetadata::getLastModificationDate;
                break;
            case NB_RECORDS:
                keyExtractor = metadata -> Optional.ofNullable(metadata)
                        .filter(m -> m.getContent() != null)
                        .map(m -> m.getContent().getNbRecords())
                        .orElse(Long.MIN_VALUE);
                break;
            default:
                // this should not be possible
                throw new TDPException(ILLEGAL_SORT_FOR_LIST, build().put("sort", sortKey));
            }
        }
        return Comparator.comparing(keyExtractor, comparisonOrder);
    }

    private static String extractDataSetName(DataSetMetadata dataSetMetadata) {
        return Optional.ofNullable(dataSetMetadata)
                .filter(p -> p.getName() != null)
                .map(p -> p.getName().toUpperCase())
                .orElse(EMPTY);
    }

    /**
     * Return a Preparation comparator from the given parameters.
     *
     * @param sortKey the sort key.
     * @param orderKey the order comparator to use.
     * @return a preparation comparator from the given parameters.
     */
    public static Comparator<PreparationDTO> getPreparationComparator(Sort sortKey, Order orderKey) {
        Comparator<Comparable> comparisonOrder = getOrderComparator(orderKey);

        // Select comparator for sort (either by name or date)
        Function<PreparationDTO, Comparable> keyExtractor;
        if (sortKey == null) { // default to NAME sort
            keyExtractor = SortAndOrderHelper::extractPreparationName;
        } else {
            switch (sortKey) {
            // In case of API call error, default to NAME sort
            case NB_RECORDS:
            case NAME:
                keyExtractor = SortAndOrderHelper::extractPreparationName;
                break;
            case AUTHOR:
                keyExtractor = preparationDTO -> Optional.ofNullable(preparationDTO)
                        .filter(p -> p.getOwner() != null)
                        .filter(p -> p.getOwner().getDisplayName() != null)
                        .map(p -> p.getOwner().getDisplayName().toUpperCase())
                        .orElse(EMPTY);
                break;
            case CREATION_DATE:
            case DATE:
                keyExtractor = PreparationDTO::getCreationDate;
                break;
            case LAST_MODIFICATION_DATE:
                keyExtractor = PreparationDTO::getLastModificationDate;
                break;
            case NB_STEPS:
                keyExtractor = preparation -> preparation.getSteps().size();
                break;
            case DATASET_NAME:
                keyExtractor = PreparationDTO::getDataSetName;
                break;
            default:
                // this should not be possible
                throw new TDPException(ILLEGAL_SORT_FOR_LIST, build().put("sort", sortKey));
            }
        }
        return Comparator.comparing(keyExtractor, comparisonOrder);
    }

    private static String extractPreparationName(PreparationDTO preparation) {
        return Optional.ofNullable(preparation)
                .filter(p -> p.getName() != null)
                .map(p -> p.getName().toUpperCase())
                .orElse(EMPTY);
    }

    /**
     * Return a Folder comparator from the given parameters.
     *
     * @param sortKey the sort key.
     * @param orderKey the order comparator to use.
     * @return a folder comparator from the given parameters.
     */
    public static Comparator<Folder> getFolderComparator(Sort sortKey, Order orderKey) {
        Comparator<Comparable> order = getOrderComparator(orderKey);

        // Select comparator for sort (either by name or date)
        Function<Folder, Comparable> keyExtractor;
        if (sortKey == null) { // default to NAME sort
            keyExtractor = SortAndOrderHelper::extractFolderName;
        } else {
            switch (sortKey) {
            // In case of API call error, default to NAME sort
            case AUTHOR:
            case DATASET_NAME:
            case NB_RECORDS:
            case NB_STEPS:
            case NAME:
                keyExtractor = SortAndOrderHelper::extractFolderName;
                break;
            case CREATION_DATE:
            case DATE:
                keyExtractor = Folder::getCreationDate;
                break;
            case LAST_MODIFICATION_DATE:
                keyExtractor = Folder::getLastModificationDate;
                break;
            default:
                // this should not be possible
                throw new TDPException(ILLEGAL_SORT_FOR_LIST, build().put("sort", sortKey));
            }
        }
        return Comparator.comparing(keyExtractor, order);
    }

    private static String extractFolderName(Folder folder) {
        return Optional.ofNullable(folder)
                .filter(f -> f.getName() != null)
                .map(f -> f.getName().toUpperCase())
                .orElse(EMPTY);
    }
}
