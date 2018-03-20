/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.dataprep.api.action;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

import java.io.*;

import org.junit.Test;

public class ActionFormTest {

    @Test
    public void testSerializable() throws IOException, ClassNotFoundException {
        ActionForm actionForm = new ActionForm();
        actionForm.setAlternateLabel("alt label");
        actionForm.setAlternateDescription("alt description");
        actionForm.setParameters(emptyList());
        actionForm.setLabel("label");
        actionForm.setDocUrl("http://docurl.talend.com/doc.html");
        actionForm.setDescription("description");
        actionForm.setCategory("category");
        actionForm.setName("toto");
        actionForm.setActionScope(emptyList());
        actionForm.setDynamic(true);

        // write object
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
        objectOutputStream.writeObject(actionForm);
        objectOutputStream.flush();

        // read object
        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
        ActionForm readActionForm = (ActionForm) objectInputStream.readObject();

        // should be equal
        assertEquals(actionForm.getAlternateLabel(), readActionForm.getAlternateLabel());
        assertEquals(actionForm.getAlternateDescription(), readActionForm.getAlternateDescription());
        assertEquals(actionForm.getParameters(), readActionForm.getParameters());
        assertEquals(actionForm.getLabel(), readActionForm.getLabel());
        assertEquals(actionForm.getDocUrl(), readActionForm.getDocUrl());
        assertEquals(actionForm.getDescription(), readActionForm.getDescription());
        assertEquals(actionForm.getCategory(), readActionForm.getCategory());
        assertEquals(actionForm.getName(), readActionForm.getName());
        assertEquals(actionForm.getActionScope(), readActionForm.getActionScope());
        assertEquals(actionForm.isDynamic(), readActionForm.isDynamic());
    }

}
