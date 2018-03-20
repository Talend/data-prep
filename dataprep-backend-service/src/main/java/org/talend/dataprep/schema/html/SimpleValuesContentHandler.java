// ============================================================================
//
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

package org.talend.dataprep.schema.html;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * ContentHandler to get values from the selector
 */
class SimpleValuesContentHandler extends DefaultHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleValuesContentHandler.class);

    private final int rowSize;

    private final long limit;

    private boolean inValue;

    private int index = -1;

    /**
     * list of row values
     */
    private List<List<String>> values = new ArrayList<>();

    SimpleValuesContentHandler(int rowSize, long limit) {
        this.rowSize = rowSize;
        this.limit = limit;
    }

    List<List<String>> getValues() {
        return values;
    }

    private List<String> getLastRow() {
        return values.get(values.size() - 1);
    }

    private boolean withinLimit() {
        return limit < 0 || values.size() < limit;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (withinLimit()) {
            if ("tr".equals(localName)) {
                // New line
                values.add(new ArrayList<>());
                index = -1;
            } else if ("td".equals(localName)) {
                inValue = true;
                getLastRow().add(StringUtils.EMPTY);
                index++;
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (withinLimit()) {
            if("tr".equals(localName) && rowSize > 0 && getLastRow().size() < rowSize) {
                // Discard row (does not match column number)
                values.remove(values.size() - 1);
            } else if ("td".equals(localName)) {
                inValue = false;
            }
        }
    }

    @Override
    public void characters(char[] chars, int start, int length) throws SAXException {
        if (withinLimit() && inValue) {
            char[] thechars = new char[length];
            System.arraycopy(chars, start, thechars, 0, length);
            String value = new String(thechars);
            LOGGER.debug("value: {}", value);
            List<String> currentRowValues = getLastRow();
            currentRowValues.set(index, StringUtils.trim(currentRowValues.get(index) + value));
        }
    }

}
