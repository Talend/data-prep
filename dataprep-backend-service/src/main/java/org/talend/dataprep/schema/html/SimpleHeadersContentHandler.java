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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX ContentHandler to get headers values. Sax is used for low memory usage, and a HeadersContentFoundException is
 * thrown when the header is found.
 */
class SimpleHeadersContentHandler extends DefaultHandler {

    /** This class logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHeadersContentHandler.class);

    /** True if reading headers (current location matches the headers location). */
    private boolean readingHeader;

    /** The headers values. */
    private List<String> headerValues = new ArrayList<>();

    /**
     * @return the header values.
     */
    List<String> getHeaderValues() {
        return headerValues;
    }

    /**
     * @see DefaultHandler#startElement(String, String, String, Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if ("th".equals(localName)) {
            readingHeader = true;
        }
    }

    /**
     * @see DefaultHandler#endElement(String, String, String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("th".equals(localName)) {
            readingHeader = false;
        }
    }

    /**
     * @see DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] chars, int start, int length) throws SAXException {
        if (readingHeader) {
            char[] thechars = new char[length];
            System.arraycopy(chars, start, thechars, 0, length);
            String headerValue = new String(thechars);
            headerValues.add(headerValue.trim());
            LOGGER.debug("header: {}", headerValue);
        }
    }

}
