package org.talend.dataprep.qa.api.step;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to store data within the same JBehave execution Thread.
 * This can be an issue
 */
public class StoryContext {

    protected static final String STORY_DATASET_UPLOADED_ID = "story.dataset.uploaded.id";
    protected static final String STORY_DATASET_UPLOADED_NAME = "story.dataset.uploaded.name";

    /**
     * Our local access to {@link ThreadLocal}.
     */
    private static ThreadLocal<StoryContext> threadContext =
            new ThreadLocal<StoryContext>();

    /**
     * Dictionary of created dataset within the local thread.
     */
    private Map<String, String> datasetById = new HashMap<>();

    /** */
    public static StoryContext context() {
        return threadContext.get();
    }

    /**
     * Store a new {@link StoryContext} for the local thread.
     */
    public static void initialize() {
        threadContext.set(new StoryContext());
    }

    /**
     * Remove the current {@link StoryContext} from the local thread.
     */
    public static void dispose() {
        threadContext.remove();
    }


    /**
     * Obtain the created dataset ordered by id.
     *
     * @return a dictionary of created dataset ordered by id.
     */
    public Map<String, String> getDatasetById() {
        return datasetById;
    }
}
