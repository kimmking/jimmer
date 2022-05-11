package org.babyfish.jimmer.sql.ast.mutation;

import org.babyfish.jimmer.sql.ast.Expression;
import org.babyfish.jimmer.sql.ast.Predicate;
import org.babyfish.jimmer.sql.ast.PropExpression;

public interface MutableUpdate {

    <X> MutableUpdate set(PropExpression<X> path, X value);

    <X> MutableUpdate set(PropExpression<X> path, Expression<X> value);

    MutableUpdate where(Predicate ... predicates);
}
