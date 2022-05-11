package org.babyfish.jimmer.apt.generator;

import com.squareup.javapoet.ClassName;
import org.babyfish.jimmer.DraftConsumer;
import org.babyfish.jimmer.apt.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.runtime.DraftContext;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

class Constants {

    public static final ClassName DRAFT_CONTEXT_CLASS_NAME =
            ClassName.get(DraftContext.class);

    public static final ClassName DRAFT_CONSUMER_CLASS_NAME =
            ClassName.get(DraftConsumer.class);

    public static final ClassName RUNTIME_TYPE_CLASS_NAME =
            ClassName.get(ImmutableType.class);

    public static final ClassName TABLE_CLASS_NAME =
            ClassName.get(
                    "org.babyfish.jimmer.sql.ast.table",
                    "Table"
            );

    public static final ClassName QUERY_TABLE_EX_CLASS_NAME =
            ClassName.get(
                    "org.babyfish.jimmer.sql.ast.table",
                    "TableEx"
            );

    public static final ClassName ABSTRACT_TABLE_WRAPPER_CLASS_NAME =
            ClassName.get(
                    "org.babyfish.jimmer.sql.ast.table.spi",
                    "AbstractTableWrapper"
            );

    public static final ClassName SQL_CLIENT_CLASS_NAME =
            ClassName.get(
                    "org.babyfish.jimmer.sql",
                    "SqlClient"
            );

    public static final ClassName FILTERABLE_CLASS_NAME =
            ClassName.get(
                    "org.babyfish.jimmer.sql.ast.query",
                    "Filterable"
            );

    public static final ClassName MUTABLE_ROOT_QUERY_CLASS_NAME =
            ClassName.get(
                    "org.babyfish.jimmer.sql.ast.query",
                    "MutableRootQuery"
            );

    public static final ClassName MUTABLE_SUB_QUERY_CLASS_NAME =
            ClassName.get(
                    "org.babyfish.jimmer.sql.ast.query",
                    "MutableSubQuery"
            );

    public static final ClassName CONFIGURABLE_TYPED_ROOT_QUERY_CLASS_NAME =
            ClassName.get(
                    "org.babyfish.jimmer.sql.ast.query",
                    "ConfigurableTypedRootQuery"
            );

    public static final ClassName CONFIGURABLE_TYPED_SUB_QUERY_CLASS_NAME =
            ClassName.get(
                    "org.babyfish.jimmer.sql.ast.query",
                    "ConfigurableTypedSubQuery"
            );

    public static final ClassName QUERIES_CLASS_NAME =
            ClassName.get(
                    "org.babyfish.jimmer.sql.ast.impl.query",
                    "Queries"
            );

    public static final ClassName BI_FUNCTION_CLASS_NAME =
            ClassName.get(BiFunction.class);

    public static final ClassName BI_CONSUMER_CLASS_NAME =
            ClassName.get(BiConsumer.class);

    public static final ClassName EXPRESSION_CLASS_NAME =
            ClassName.get(
                    "org.babyfish.jimmer.sql.ast",
                    "Expression"
            );

    public static final ClassName PROP_STRING_EXPRESSION_CLASS_NAME =
            ClassName.get(
                    "org.babyfish.jimmer.sql.ast",
                    "PropExpression",
                    "Str"
            );

    public static final ClassName PROP_NUMERIC_EXPRESSION_CLASS_NAME =
            ClassName.get(
                    "org.babyfish.jimmer.sql.ast",
                    "PropExpression",
                    "Num"
            );

    public static final ClassName PROP_COMPARABLE_EXPRESSION_CLASS_NAME =
            ClassName.get(
                    "org.babyfish.jimmer.sql.ast",
                    "PropExpression",
                    "Cmp"
            );

    public static final String DRAFT_FIELD_CTX =
            "__ctx";

    public static final String DRAFT_FIELD_BASE =
            "__base";

    public static final String DRAFT_FIELD_MODIFIED =
            "__modified";

    public static final String DRAFT_FIELD_RESOLVING =
            "__resolving";

    public static final String DRAFT_FIELD_EMAIL_PATTERN =
            "__email_pattern";

    public static String regexpPatternFieldName(ImmutableProp prop, int index) {
        return "__" + prop.getName() + "_pattern" + (index == 0 ? "" : "_" + index);
    }
}
