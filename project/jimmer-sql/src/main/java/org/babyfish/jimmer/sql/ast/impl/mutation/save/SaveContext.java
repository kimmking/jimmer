package org.babyfish.jimmer.sql.ast.impl.mutation.save;

import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.runtime.ImmutableSpi;
import org.babyfish.jimmer.sql.JoinSql;
import org.babyfish.jimmer.sql.ManyToOne;
import org.babyfish.jimmer.sql.OneToMany;
import org.babyfish.jimmer.sql.OneToOne;
import org.babyfish.jimmer.sql.ast.impl.mutation.SaveOptions;
import org.babyfish.jimmer.sql.ast.mutation.AffectedTable;
import org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.babyfish.jimmer.sql.meta.IdGenerator;
import org.babyfish.jimmer.sql.meta.UserIdGenerator;
import org.babyfish.jimmer.sql.meta.impl.IdentityIdGenerator;
import org.babyfish.jimmer.sql.meta.impl.SequenceIdGenerator;
import org.babyfish.jimmer.sql.runtime.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

class SaveContext {

    final SaveOptions options;

    final Connection con;

    final MutationTrigger2 trigger;

    final Map<AffectedTable, Integer> affectedRowCountMap;

    final MutationPath path;

    final ImmutableProp backReferenceProp;

    final boolean backReferenceFrozen;

    SaveContext(
            SaveOptions options,
            Connection con,
            ImmutableType type
    ) {
        this(
                options,
                con,
                type,
                options.getTriggers() != null ? new MutationTrigger2() : null,
                new LinkedHashMap<>()
        );
    }

    SaveContext(
            SaveOptions options,
            Connection con,
            ImmutableType type,
            MutationTrigger2 trigger,
            Map<AffectedTable, Integer> affectedRowCountMap
    ) {
        this.options = options;
        this.con = con;
        this.trigger = trigger;
        this.affectedRowCountMap = affectedRowCountMap;
        this.path = MutationPath.root(type);
        this.backReferenceProp = null;
        this.backReferenceFrozen = false;
    }

    private SaveContext(SaveContext base, ImmutableProp prop, ImmutableProp backProp) {
        if (prop == null) {
            prop = backProp.getOpposite();
        } else {
            backProp = prop.getOpposite();
        }
        this.options = base.options.toMode(
                prop != null ?
                        base.options.getAssociatedMode(prop) == AssociatedSaveMode.APPEND ?
                                SaveMode.INSERT_ONLY :
                                SaveMode.UPSERT :
                        SaveMode.UPSERT
        );
        this.con = base.con;
        this.trigger = base.trigger;
        this.affectedRowCountMap = base.affectedRowCountMap;
        this.path = prop != null ? base.path.to(prop) : base.path.backFrom(backProp);
        if (prop != null && prop.getAssociationAnnotation().annotationType() == OneToMany.class) {
            this.backReferenceProp = prop.getMappedBy();
            this.backReferenceFrozen = !((OneToMany)prop.getAssociationAnnotation()).isTargetTransferable();
        } else {
            this.backReferenceProp = backProp;
            this.backReferenceFrozen = false;
        }
    }

    public Object allocateId() {
        IdGenerator idGenerator = options.getSqlClient().getIdGenerator(path.getType().getJavaClass());
        if (idGenerator == null) {
            throw new SaveException.NoIdGenerator(
                    path,
                    "Cannot save \"" +
                            path.getType() + "\" " +
                            "without id because id generator is not specified"
            );
        }
        JSqlClientImplementor sqlClient = options.getSqlClient();
        if (idGenerator instanceof SequenceIdGenerator) {
            String sql = sqlClient.getDialect().getSelectIdFromSequenceSql(
                    ((SequenceIdGenerator)idGenerator).getSequenceName()
            );
            return sqlClient.getExecutor().execute(
                    new Executor.Args<>(
                            sqlClient,
                            con,
                            sql,
                            Collections.emptyList(),
                            sqlClient.getSqlFormatter().isPretty() ? Collections.emptyList() : null,
                            ExecutionPurpose.MUTATE,
                            null,
                            stmt -> {
                                try (ResultSet rs = stmt.executeQuery()) {
                                    rs.next();
                                    return rs.getObject(1);
                                }
                            }
                    )
            );
        }
        if (idGenerator instanceof UserIdGenerator<?>) {
            return ((UserIdGenerator<?>)idGenerator).generate(path.getType().getJavaClass());
        }
        if (idGenerator instanceof IdentityIdGenerator) {
            return null;
        }
        throw new SaveException.IllegalIdGenerator(
                path,
                "Illegal id generator type: \"" +
                        idGenerator.getClass().getName() +
                        "\", id generator must be sub type of \"" +
                        SequenceIdGenerator.class.getName() +
                        "\", \"" +
                        IdentityIdGenerator.class.getName() +
                        "\" or \"" +
                        UserIdGenerator.class.getName() +
                        "\""
        );
    }

    public SaveContext prop(ImmutableProp prop) {
        return new SaveContext(this, prop, null);
    }

    public SaveContext backProp(ImmutableProp backProp) {
        return new SaveContext(this, null, backProp);
    }

    void throwOptimisticLockError() {
        throw new SaveException.OptimisticLockError(
                path,
                "Cannot update the entity whose type is \"" +
                        path.getType() +
                        "\" with version " +
                        "\" when using optimistic lock"
        );
    }

    void throwOptimisticLockError(ImmutableSpi row) {
        throw new SaveException.OptimisticLockError(
                path,
                "Cannot update the entity whose type is \"" +
                        path.getType() +
                        "\" and id is \"" +
                        row.__get(path.getType().getIdProp().getId()) +
                        "\" because of optimistic lock error"
        );
    }

    void throwReadonlyMiddleTable() {
        throw new SaveException.ReadonlyMiddleTable(
                path,
                "The property \"" +
                        path.getProp() +
                        "\" which is based on readonly middle table cannot be saved"
        );
    }

    void throwReversedRemoteAssociation() {
        throw new SaveException.ReversedRemoteAssociation(
                path,
                "The property \"" +
                        path.getProp() +
                        "\" which is reversed(with `mappedBy`) remote(across different microservices) association " +
                        "cannot be supported by save command"
        );
    }

    void throwUnstructuredAssociation() {
        throw new SaveException.UnstructuredAssociation(
                path,
                "The property \"" +
                        path.getProp() +
                        "\" which is unstructured association(decorated by @" +
                        JoinSql.class.getName() +
                        ") " +
                        "cannot be supported by save command"
        );
    }

    void throwIllegalTargetIds(Collection<Object> illegalTargetIds) {
        if (!illegalTargetIds.isEmpty()) {
            throw new SaveException.IllegalTargetId(
                    path,
                    "Illegal ids: " + illegalTargetIds
            );
        }
    }

    void throwNoIdGenerator() {
        throw new SaveException.NoIdGenerator(
                path,
                "Cannot save \"" +
                        path.getType() + "\" " +
                        "without id because id generator is not specified"
        );
    }

    void throwNeitherIdNorKey(ImmutableSpi spi, ImmutableProp keyProp) {
        throw new SaveException.NeitherIdNorKey(
                path,
                "Cannot save illegal entity object " +
                        spi +
                        " whose type is \"" +
                        spi.__type() +
                        "\", key property \"" +
                        keyProp +
                        "\" must be loaded when id is unloaded"
        );
    }

    void throwFailedRemoteValidation() {
        throw new SaveException.FailedRemoteValidation(
                path,
                "Cannot validate the id-only associated objects of remote association \"" +
                        path.getProp() +
                        "\""
        );
    }

    void throwLongRemoteAssociation() {
        throw new SaveException.LongRemoteAssociation(
                path,
                "The property \"" +
                        path.getProp() +
                        "\" is remote(across different microservices) association, " +
                        "but it has associated object which is not id-only"
        );
    }

    void throwNullTarget() {
        throw new SaveException.NullTarget(
                path,
                "The association \"" +
                        path.getProp() +
                        "\" cannot be null, because that association is decorated by \"@" +
                        (path.getProp().getAnnotation(ManyToOne.class) != null ? ManyToOne.class : OneToOne.class).getName() +
                        "\" whose `inputNotNull` is true"
        );
    }
}
