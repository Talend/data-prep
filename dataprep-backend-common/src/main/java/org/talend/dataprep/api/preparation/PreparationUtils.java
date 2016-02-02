//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.preparation;

import static java.util.stream.Collectors.toList;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.preparation.store.PreparationRepository;

@Component
public class PreparationUtils {

    /** The default root content. */
    @Resource(name = "rootContent")
    private PreparationActions rootContent;

    /**
     * Return a list of all steps ids from root step to the provided step
     * @param stepId        The last step id to get
     * @param repository    The identifiable repository
     * @return The list of step ids from root to step
     */
    public List<String> listStepsIds(final String stepId, final PreparationRepository repository) {
        return listStepsIds(stepId, rootContent.getId(), repository);
    }

    /**
     * Return a list of all steps ids from limit step to the provided step
     * @param stepId        The last step to get
     * @param limit         The starting step
     * @param repository    The identifiable repository
     * @return The list of step ids from starting (limit) to step
     */
    public List<String> listStepsIds(final String stepId, final String limit, final PreparationRepository repository) {
        return listSteps(repository.get(stepId, Step.class), limit, repository).stream()
                .map(Step::id)
                .collect(toList());
    }

    /**
     * Returns a list of all steps available from <code>step</code> parameter. Since all preparations share the same
     * root, calling this method is equivalent to:
     * <code>
     * listSteps(step, PreparationActions.ROOT_CONTENT.getId(), repository);
     * </code>
     *
     * @param headStepId The head step id.
     * @param repository A {@link PreparationRepository version} repository.
     * @return A list of {@link Step step} id. Empty list if <code>step</code> parameter is <code>null</code>.
     * @see Step#id()
     * @see Step#getParent()
     */
    public List<Step> listSteps(String headStepId, PreparationRepository repository) {
        return listSteps(repository.get(headStepId, Step.class), rootContent.getId(), repository);
    }

    /**
     * Returns a list of all steps available from <code>step</code> parameter.
     *
     * @param step A {@link Step step}.
     * @param limit An {@link Step step } id limit for the steps history. History will stop at {@link Step step} with
     *              this id.
     * @param repository A {@link PreparationRepository version} repository.
     * @return A list of {@link Step step} id. Empty list if <code>step</code> parameter is <code>null</code>.
     * @see Step#id()
     * @see Step#getParent()
     */
    public List<Step> listSteps(final Step step, final String limit, final PreparationRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Repository cannot be null.");
        }
        if (limit == null) {
            throw new IllegalArgumentException("Limit cannot be null.");
        }
        if (step == null) {
            return Collections.emptyList();
        }
        final List<Step> steps = new LinkedList<>();
        __listSteps(steps, limit, step, repository);
        return steps;
    }

    private static void __listSteps(final List<Step> steps, final String limit, final Step step, final PreparationRepository repository) {
        if (step == null) {
            return;
        }
        steps.add(0, step);
        if (limit.equals(step.getId())) {
            return;
        }
        __listSteps(steps, limit, repository.get(step.getParent(), Step.class), repository);
    }

    private static void prettyPrint(PreparationRepository repository, String stepId, OutputStream out) {
        if (stepId == null) {
            return;
        }
        try {
            Step step = repository.get(stepId, Step.class);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.append("\t\tStep (").append(step.id()).append(")").append("\n");
            writer.flush();
            PreparationActions blob = repository.get(step.getContent(), PreparationActions.class);
            prettyPrint(blob, out);
            prettyPrint(repository, step.getParent(), out);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_PRINT_PREPARATION, e);
        }
    }

    private static void prettyPrint(PreparationActions blob, OutputStream out) {
        if (blob == null) {
            return;
        }
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.append("\t\t\tContent: ").append("\n");
            writer.append("======").append("\n");
            writer.append(blob.serializeActions()).append("\n");
            writer.append("======").append("\n");
            writer.flush();
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_PRINT_PREPARATION, e);
        }
    }

    public static void prettyPrint(PreparationRepository repository, Preparation preparation, OutputStream out) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.append("Preparation (").append(preparation.id()).append(")\n");
            writer.append("\tData set: ").append(preparation.getDataSetId()).append("\n");
            writer.append("\tAuthor: ").append(preparation.getAuthor()).append("\n");
            writer.append("\tCreation date: ").append(String.valueOf(preparation.getCreationDate())).append("\n");
            writer.append("\tSteps:").append("\n");
            writer.flush();
            prettyPrint(repository, preparation.getHeadId(), out);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_PRINT_PREPARATION, e);
        }
    }
}
