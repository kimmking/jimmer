package org.babyfish.jimmer.sql.ast.impl.mutation;

import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.ast.mutation.LockMode;

import java.util.Set;

class SaverCache extends MutationCache {

    private AbstractEntitySaveCommandImpl.Data data;

    public SaverCache(AbstractEntitySaveCommandImpl.Data data) {
        super(data.getSqlClient(), data.getLockMode() == LockMode.PESSIMISTIC);
        this.data = data;
    }

    @Override
    protected Set<ImmutableProp> keyProps(ImmutableType type) {
        return data.getKeyProps(type);
    }
}
