// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.service.delegate;

import java.util.stream.Stream;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.service.api.EnrichedPreparation;
import org.talend.dataprep.api.service.command.preparation.LocatePreparation;
import org.talend.dataprep.api.service.command.preparation.PreparationSearchByName;
import org.talend.dataprep.command.CommandHelper;
import org.talend.dataprep.preparation.service.UserPreparation;

/**
 * A {@link SearchDelegate} implementation to search in preparations.
 */
@Component
public class PreparationSearchDelegate extends AbstractSearchDelegate<EnrichedPreparation> {

    @Override
    public String getSearchCategory() {
        return "preparations";
    }

    @Override
    public Stream<EnrichedPreparation> search(String query, boolean strict) {
        final PreparationSearchByName command = getCommand(PreparationSearchByName.class, query, strict);
        return CommandHelper.toStream(UserPreparation.class, mapper, command).map(this::locatePreparation);
    }

    /**
     * Find where the preparation is located.
     *
     * @param preparation the preparation to locate.
     * @return an enriched preparation with additional folder parameters.
     */
    private EnrichedPreparation locatePreparation(UserPreparation preparation) {
        final LocatePreparation command = getCommand(LocatePreparation.class, preparation.id());
        final Folder folder = command.execute();
        return new EnrichedPreparation(preparation, folder);
    }

}
