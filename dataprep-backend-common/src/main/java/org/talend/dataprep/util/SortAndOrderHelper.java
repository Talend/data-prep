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

import static java.lang.String.valueOf;
import static org.talend.daikon.exception.ExceptionContext.build;

import java.util.Comparator;

import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

/**
 * Utility class used to sort and order DataSets or Preparations.
 */
public class SortAndOrderHelper {

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
        }
        catch (IllegalArgumentException e) {
            throw new TDPException(CommonErrorCodes.ILLEGAL_ORDER_FOR_LIST, build().put("order", orderKey));
        }

        switch (order) {
            case ASC:
                comparisonOrder = Comparator.naturalOrder();
                break;
            case DESC:
                comparisonOrder = Comparator.reverseOrder();
                break;
            default :
                // this should not happen
                throw new TDPException(CommonErrorCodes.ILLEGAL_ORDER_FOR_LIST, build().put("order", orderKey));

        }
        return comparisonOrder;
    }

    /**
     * Return a dataset metadata comparator from the given parameters.
     *
     * @param sortKey the sort key.
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
        }
        catch (IllegalArgumentException e) {
            throw new TDPException(CommonErrorCodes.ILLEGAL_ORDER_FOR_LIST, build().put("sort", sortKey));
        }

        switch (sort) {
            case NAME:
                comparator = Comparator.comparing(dataSetMetadata -> dataSetMetadata.getName().toUpperCase(), comparisonOrder);
                break;
            case DATE:
                comparator = Comparator.comparing(metadata -> valueOf(metadata.getCreationDate()), comparisonOrder);
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
     * @param sortKey the sort key.
     * @param orderKey the order comparator to use.
     * @return a dataset metadata comparator from the given parameters.
     */
    public static Comparator<Preparation> getPreparationComparator(String sortKey, String orderKey) {

        Comparator<String> comparisonOrder = getOrderComparator(orderKey);

        Sort sort;
        try {
            sort = Sort.valueOf(sortKey.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            throw new TDPException(CommonErrorCodes.ILLEGAL_ORDER_FOR_LIST, build().put("sort", sortKey));
        }

        // Select comparator for sort (either by name or date)
        final Comparator<Preparation> comparator;
        switch (sort) {
            case NAME:
                comparator = Comparator.comparing(Preparation::getName, comparisonOrder);
                break;
            case DATE:
                comparator = Comparator.comparing(p -> String.valueOf(p.getCreationDate()), comparisonOrder);
                break;
            case MODIF:
                comparator = Comparator.comparing(p -> String.valueOf(p.getLastModificationDate()), comparisonOrder);
                break;
            default:
                throw new TDPException(CommonErrorCodes.ILLEGAL_SORT_FOR_LIST, ExceptionContext.build().put("sort", sort));
        }
        return comparator;
    }
}