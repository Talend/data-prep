package org.talend.dataprep.filter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.tql.model.*;
import org.talend.tql.visitor.IASTVisitor;

public class ObjectPredicateVisitor implements IASTVisitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectPredicateVisitor.class);

    private final Class targetClass;

    public ObjectPredicateVisitor(Class targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public Predicate<DataSetMetadata> visit(TqlElement tqlElement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Void visit(ComparisonOperator comparisonOperator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String visit(LiteralValue literalValue) {
        return literalValue.getValue();
    }

    @Override
    public Method[] visit(FieldReference fieldReference) {
        return getMethods(fieldReference);
    }

    private Method[] getMethods(FieldReference fieldReference) {
        return getMethods(fieldReference.getPath());
    }

    private Method[] getMethods(String field) {
        StringTokenizer tokenizer = new StringTokenizer(field, ".");
        List<String> methodNames = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            methodNames.add(tokenizer.nextToken());
        }

        Class currentClass = targetClass;
        Method[] methods = new Method[methodNames.size()];
        for (int i = 0; i < methodNames.size(); i++) {
            String[] getterCandidates = new String[] {
                "get" + WordUtils.capitalize(methodNames.get(i)), //
                methodNames.get(i), //
                "is" + WordUtils.capitalize(methodNames.get(i))
            };

            for (String getterCandidate : getterCandidates) {
                try {
                    methods[i] = currentClass.getMethod(getterCandidate);
                    break;
                } catch (Exception e) {
                    LOGGER.debug("Can't find getter '{}'.", field, e);
                }
            }
            if (methods[i] == null) {
                throw new UnsupportedOperationException("Can't find getter '" + field + "'.");
            } else {
                currentClass = methods[i].getReturnType();
            }
        }
        return methods;
    }

    private Object invoke(Object o, Method[] methods) throws InvocationTargetException, IllegalAccessException {
        Object currentObject = o;
        for (Method method : methods) {
            currentObject = method.invoke(currentObject);
        }
        return currentObject;
    }

    @Override
    public Predicate<Object> visit(Expression expression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Predicate<Object> visit(AndExpression andExpression) {
        final Expression[] expressions = andExpression.getExpressions();
        if (expressions.length > 0) {
            Predicate<Object> predicate = (Predicate<Object>) expressions[0].accept(this);
            for (int i = 1; i < expressions.length; i++) {
                predicate = predicate.and((Predicate<Object>) expressions[i].accept(this));
            }
            return predicate;
        } else {
            return m -> true;
        }
    }

    @Override
    public Predicate<Object> visit(OrExpression orExpression) {
        final Expression[] expressions = orExpression.getExpressions();
        if (expressions.length > 0) {
            Predicate<Object> predicate = (Predicate<Object>) expressions[0].accept(this);
            for (int i = 1; i < expressions.length; i++) {
                predicate = predicate.or((Predicate<Object>) expressions[i].accept(this));
            }
            return predicate;
        } else {
            return m -> true;
        }
    }

    @Override
    public Predicate<Object> visit(ComparisonExpression comparisonExpression) {
        final Object value = comparisonExpression.getValueOrField().accept(this);
        final Method[] getters = (Method[]) comparisonExpression.getField().accept(this);
        final ComparisonOperator operator = comparisonExpression.getOperator();
        switch (operator.getOperator()) {
        case EQ:
            return eq(value, getters);
        case LT:
            return lt(value, getters);
        case GT:
            return gt(value, getters);
        case NEQ:
            return neq(value, getters);
        case LET:
        case GET:
        default:
            throw new UnsupportedOperationException();
        }

    }

    private Predicate<Object> neq(Object value, Method[] getters) {
        return m -> {
            try {
                return !ObjectUtils.equals(invoke(m, getters), value);
            } catch (Exception e) {
                LOGGER.error("Unable to use NEQ for '{}' with '{}'.", getters, value, e);
                return false;
            }
        };
    }

    private Predicate<Object> gt(Object value, Method[] getters) {
        return m -> {
            try {
                return Double.parseDouble(String.valueOf(invoke(m, getters))) > Double.parseDouble(String.valueOf(value));
            } catch (Exception e) {
                LOGGER.error("Unable to use GT for '{}' with '{}'.", getters, value, e);
                return false;
            }
        };
    }

    private Predicate<Object> lt(Object value, Method[] getters) {
        return m -> {
            try {
                return Double.parseDouble(String.valueOf(invoke(m, getters))) < Double.parseDouble(String.valueOf(value));
            } catch (Exception e) {
                LOGGER.error("Unable to use LT for '{}' with '{}'.", getters, value, e);
                return false;
            }
        };
    }

    private Predicate<Object> eq(Object value, Method[] getters) {
        return m -> {
            try {
                return StringUtils.equalsIgnoreCase(String.valueOf(invoke(m, getters)), String.valueOf(value));
            } catch (Exception e) {
                LOGGER.error("Unable to use EQ for '{}' with '{}'.", getters, value, e);
                return false;
            }
        };
    }

    @Override
    public Predicate<Object> visit(FieldInExpression fieldInExpression) {
        final Method[] methods = getMethods(fieldInExpression.getFieldName());
        final LiteralValue[] values = fieldInExpression.getValues();
        if (values.length > 0) {
            Predicate<Object> predicate = eq(values[0].accept(this), methods);
            for (int i = 1; i < values.length; i++) {
                predicate = predicate.or(eq(values[i].accept(this), methods));
            }
            return predicate;
        } else {
            return m -> true;
        }
    }

    @Override
    public Predicate<Object> visit(FieldIsEmptyExpression fieldIsEmptyExpression) {
        final Method[] getters = getMethods(fieldIsEmptyExpression.getFieldName());
        return o -> {
            try {
                return StringUtils.isEmpty(String.valueOf(invoke(o, getters)));
            } catch (Exception e) {
                LOGGER.error("Unable to use EMPTY for '{}'.", getters, e);
                return false;
            }
        };
    }

    @Override
    public Object visit(FieldIsValidExpression fieldIsValidExpression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(FieldIsInvalidExpression fieldIsInvalidExpression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Predicate<Object> visit(FieldMatchesRegex fieldMatchesRegex) {
        final Method[] methods = getMethods(fieldMatchesRegex.getFieldName());
        final Pattern pattern = Pattern.compile(fieldMatchesRegex.getRegex());
        return o -> {
            try {
                return pattern.matcher(String.valueOf(invoke(o, methods))).matches();
            } catch (Exception e) {
                LOGGER.error("Unable to use MATCHES for '{}'.", methods, e);
                return false;
            }
        };
    }

    @Override
    public Predicate<Object> visit(FieldCompliesPattern fieldCompliesPattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Predicate<Object> visit(FieldBetweenExpression fieldBetweenExpression) {
        final Method[] method = getMethods(fieldBetweenExpression.getFieldName());
        return gt(fieldBetweenExpression.getLeft().accept(this), method)
                .and(lt(fieldBetweenExpression.getRight().accept(this), method));
    }

    @Override
    public Predicate<Object> visit(NotExpression notExpression) {
        final Predicate<Object> accept = (Predicate<Object>) notExpression.getExpression().accept(this);
        return m -> !accept.test(m);
    }

    @Override
    public Predicate<Object> visit(FieldContainsExpression fieldContainsExpression) {
        final Method[] methods = getMethods(fieldContainsExpression.getFieldName());
        return o -> {
            try {
                return StringUtils.containsIgnoreCase(String.valueOf(invoke(o, methods)), fieldContainsExpression.getValue());
            } catch (Exception e) {
                LOGGER.error("Unable to use CONTAINS for '{}'.", methods, e);
                return false;
            }
        };
    }
}
