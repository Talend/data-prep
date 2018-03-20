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

package org.talend.dataprep.api.type;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = Type.TypeSerializer.class)
public enum Type implements Serializable {

    ANY("any", "ANY"), //$NON-NLS-1$
    STRING("string", ANY, "STRING"), //$NON-NLS-1$
    BOOLEAN("boolean", STRING, "BOOLEAN"), //$NON-NLS-1$
    DATE("date", STRING, "DATE"), //$NON-NLS-1$
    UTC_DATETIME("utc_datetime", STRING, "UTC_DATETIME"), //$NON-NLS-1$
    NUMERIC("numeric", STRING, "NUMERIC"), //$NON-NLS-1$
    DOUBLE("double", NUMERIC, "DOUBLE"), //$NON-NLS-1$
    INTEGER("integer", NUMERIC, "INTEGER"), //$NON-NLS-1$
    FLOAT("float", NUMERIC, "FLOAT"); //$NON-NLS-1$

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    /** The type name. */
    private final String name;

    /** Can be used as a label key in the ui. */
    private final String labelKey;

    /** Super type. (e.g. ANY > NUMERIC > INTEGER). */
    private final Type superType;

    /** Subtypes. (e.g. INTEGER < NUMERIC < ANY) */
    private final List<Type> subTypes = new LinkedList<>();

    /**
     * Create a root type without any super type.
     *
     * @param name the type name.
     * @param labelKey the label.
     */
    Type(String name, String labelKey) {
        this(name, null, labelKey);
    }

    /**
     * Create a type.
     *
     * @param name the type name.
     * @param superType the super type.
     * @param labelKey the label for the UI.
     */
    Type(String name, Type superType, String labelKey) {
        this.name = name;
        this.superType = superType;
        this.labelKey = labelKey;
        if (superType != null) {
            superType.declareSubType(this);
        }
    }

    /**
     * Add the given type as a subtype.
     *
     * @param type the sub type to add.
     */
    void declareSubType(Type type) {
        subTypes.add(type);
    }

    /**
     * @return A unique type name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The type's super type, returned value is never null (top level type is returns itself). All other types
     * are expected at least to return {@link Type#ANY}.
     * @see Type#ANY
     */
    public Type getSuperType() {
        return superType;
    }

    /**
     * @return the type label for the UI.
     */
    private String getLabelKey() {
        return labelKey;
    }

    /**
     * Returns the type hierarchy starting from this type. Calling this method on {@link Type#ANY} returns all supported
     * types.
     *
     * @return The list of types assignable from this type, including this type (i.e. . Returned list is never empty
     * since it at least <code>this</code>.
     */
    public List<Type> list() {
        List<Type> list = new LinkedList<>();
        list.add(this);
        subTypes.forEach(type -> list.addAll(type.list()));
        return list;
    }

    /**
     * @return the type name.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns whether <code>type</code> is contained in the list of subtypes from {@link Type#list()}.
     *
     * @param type The type to test
     * @return true is <code>type</code> is contained in all subtypes including this type or false otherwise.
     */
    public boolean isAssignableFrom(Type type) {
        return list().contains(type);
    }

    /**
     * Returns whether <code>type</code> is contained in the list of subtypes from {@link Type#list()}.
     *
     * @param type The type name to test
     * @return true is <code>type</code> is contained in all subtypes including this type or false otherwise.
     */
    public boolean isAssignableFrom(String type) {
        return list().contains(Type.get(type));
    }

    /**
     * Returns the type accessible from {@link Type#ANY} with <code>name</code>.
     *
     * @param name A non-null type name.
     * @return The {@link Type} type corresponding to <code>name</code>.
     * @throws IllegalArgumentException If <code>name</code> is <code>null</code> or if type does not exist.
     */
    public static Type get(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null.");
        }
        List<Type> types = ANY.list();

        Optional<Type> type = types.stream().filter(type1 -> type1.getName().equalsIgnoreCase(name)).findFirst();

        if (type.isPresent()) {
            return type.get();
        }

        // default type to String
        return STRING;
    }

    /**
     * Allows to test existence of a type by name.
     *
     * @param name A non-null type name.
     * @return <code>true</code> if type exists, <code>false</code> otherwise.
     * @see #get(String)
     */
    public static boolean has(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null.");
        }
        List<Type> types = ANY.list();
        Optional<Type> type = types.stream().filter(type1 -> type1.getName().equalsIgnoreCase(name)).findFirst();

        return type.isPresent();
    }

    public static class TypeSerializer extends JsonSerializer<Type> {

        @Override
        public void serialize(Type value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartObject();

            jgen.writeStringField("id", value.name());
            jgen.writeStringField("name", value.getName());
            jgen.writeStringField("labelKey", value.getLabelKey());

            jgen.writeEndObject();
        }
    }
}
