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

package org.talend.dataprep.util;

import org.apache.commons.lang.StringUtils;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.Comparator;

import static java.lang.String.valueOf;
import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.exception.error.CommonErrorCodes.ILLEGAL_SORT_FOR_LIST;

/**
 * Utility class used to sort and order DataSets or Preparations.
 */
public class SortAndOrderHelper {

    private static class SortPropertyEditor extends PropertyEditorSupport {

        @Override
        public void setAsText(String text) {
            setValue(Sort.valueOf(text.toUpperCase()));
        }
    }

    private static class OrderPropertyEditor extends PropertyEditorSupport {

        @Override
        public void setAsText(String text) {
            setValue(Order.valueOf(text.toUpperCase()));
        }
    }

    /**
     * Create a {@link PropertyEditor} to allow binding of lower-case {@link Order} in
     * {@link org.springframework.web.bind.annotation.RequestParam @RequestParam}.
     */
    public static PropertyEditor getOrderPropertyEditor() {
        return new OrderPropertyEditor();
    }

    /**
     * Create a {@link PropertyEditor} to allow binding of lower-case {@link Sort} in
     * {@link org.springframework.web.bind.annotation.RequestParam @RequestParam}.
     */
    public static PropertyEditor getSortPropertyEditor() {
        return new SortPropertyEditor();
    }

    /** Order to apply to a sort. */
    public enum Order {
        /** Ascending order. */
        ASC,
        /** Descending order. */
        DESC
    }

    /** How to sort things. */
    public enum Sort {
        /** Date of creation. */
        DATE,
        /** Name. */
        NAME,
        /** Last modification date. */
        MODIF
    }

    /**
     * Return the string comparator to use for the given order name.
     *
     * @param orderKey the name of the order to apply.
     * @return the string comparator to use for the given order name.
     */
    private static Comparator<String> getOrderComparator(String orderKey) {

        final Comparator<String> comparisonOrder;

        // Select order (asc or desc)
        final Order order;
        try {
            order = Order.valueOf(orderKey.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new TDPException(CommonErrorCodes.ILLEGAL_ORDER_FOR_LIST, build().put("order", orderKey));
        }

        switch (order) {
        case ASC:
            comparisonOrder = Comparator.naturalOrder();
            break;
        case DESC:
            comparisonOrder = Comparator.reverseOrder();
            break;
        default:
            // this should not happen
            throw new TDPException(CommonErrorCodes.ILLEGAL_ORDER_FOR_LIST, build().put("order", orderKey));
        }
        return comparisonOrder;
    }

    /**
     * Return a dataset metadata comparator from the given parameters.
     *
     * @param sortKey  the sort key.
     * @param orderKey the order key to use.
     * @return a dataset metadata comparator from the given parameters.
     */
    public static Comparator<DataSetMetadata> getDataSetMetadataComparator(String sortKey, String orderKey) {

        Comparator<String> comparisonOrder = getOrderComparator(orderKey);

        // Select comparator for sort (either by name or date)
        final Comparator<DataSetMetadata> comparator;

        Sort sort;
        try {
            sort = Sort.valueOf(sortKey.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new TDPException(CommonErrorCodes.ILLEGAL_ORDER_FOR_LIST, build().put("sort", sortKey));
        }

        switch (sort) {
        case NAME:
            comparator = Comparator.comparing(dataSetMetadata -> dataSetMetadata.getName().toUpperCase(), comparisonOrder);
            break;
        case DATE:
            comparator = Comparator.comparing(metadata -> valueOf(metadata.getCreationDate()), comparisonOrder);
            break;
        case MODIF:
            comparator = Comparator.comparing(metadata -> valueOf(metadata.getLastModificationDate()), comparisonOrder);
            break;
        default:
            // this should never happen
            throw new TDPException(CommonErrorCodes.ILLEGAL_ORDER_FOR_LIST, build().put("sort", sort));
        }
        return comparator;
    }

    /**
     * Return a Preparation comparator from the given parameters.
     *
     * @param sortKey  the sort key.
     * @param orderKey the order comparator to use.
     * @return a preparation comparator from the given parameters.
     */
    public static Comparator<Preparation> getPreparationComparator(String sortKey, String orderKey) {

        Comparator<String> comparisonOrder = getOrderComparator(orderKey);

        Sort sort;
        try {
            sort = Sort.valueOf(sortKey.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new TDPException(CommonErrorCodes.ILLEGAL_ORDER_FOR_LIST, build().put("sort", sortKey));
        }

        // Select comparator for sort (either by name or date)
        final Comparator<Preparation> comparator;
        switch (sort) {
        case NAME:
            comparator = Comparator.comparing(dataSetMetadata -> dataSetMetadata.getName().toUpperCase(), comparisonOrder);
            break;
        case DATE:
            comparator = Comparator.comparing(p -> {
                if (p != null) {
                    return String.valueOf(p.getCreationDate());
                } else {
                    return StringUtils.EMPTY;
                }
            }, comparisonOrder);
            break;
        case MODIF:
            comparator = Comparator.comparing(p -> {
                if (p != null) {
                    return String.valueOf(p.getLastModificationDate());
                } else {
                    return StringUtils.EMPTY;
                }
            }, comparisonOrder);
            break;
        default:
            throw new TDPException(ILLEGAL_SORT_FOR_LIST, ExceptionContext.build().put("sort", sort));
        }
        return comparator;
    }

    /**
     * Return a Folder comparator from the given parameters.
     *
     * @param sortKey  the sort key.
     * @param orderKey the order comparator to use.
     * @return a folder comparator from the given parameters.
     */
    public static Comparator<Folder> getFolderComparator(String sortKey, String orderKey) {

        Comparator<String> order = getOrderComparator(orderKey);

        Sort sort;
        try {
            sort = Sort.valueOf(sortKey.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new TDPException(CommonErrorCodes.ILLEGAL_ORDER_FOR_LIST, build().put("sort", sortKey));
        }

        // Select comparator for sort (either by name or date)
        final Comparator<Folder> comp;
        switch (sort) {
        case NAME:
            comp = Comparator.comparing(Folder::getName, order);
            break;
        case DATE:
            comp = Comparator.comparing(folder -> String.valueOf(folder.getCreationDate()), order);
            break;
        case MODIF:
            comp = Comparator.comparing(folder -> String.valueOf(folder.getLastModificationDate()), order);
            break;
        default:
            throw new TDPException(ILLEGAL_SORT_FOR_LIST, ExceptionContext.build().put("sort", sort));
        }
        return comp;
    }
}