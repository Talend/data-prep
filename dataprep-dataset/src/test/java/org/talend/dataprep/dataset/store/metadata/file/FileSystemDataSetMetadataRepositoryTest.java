package org.talend.dataprep.dataset.store.metadata.file;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.TestPropertySource;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.dataset.DataSetBaseTest;

/**
 * Unit/integration test for the file system dataset metadata repository.
 */
@TestPropertySource(properties = { "dataset.metadata.store=file",
        "dataset.metadata.store.file.location=target/test/store/metadata" })
public class FileSystemDataSetMetadataRepositoryTest extends DataSetBaseTest {

    /** Where to store the dataset metadata. */
    @Value("${dataset.metadata.store.file.location}")
    private String storeLocation;

    /** The repository to test. */
    @Autowired
    private FileSystemDataSetMetadataRepository repository;

    /** DataPrep jackson ready to use builder. */
    @Autowired
    Jackson2ObjectMapperBuilder builder;

    @After
    public void clear() {
        repository.clear();
    }

    @Test
    public void getShouldReturnSameMetadataThatWasAdded() throws IOException {
        // given
        final DataSetMetadata expected = getMetadata("456789");

        // when
        repository.add(expected);
        final DataSetMetadata actual = repository.get(expected.getId());

        // then
        assertEquals(expected, actual);
    }

    @Test
    public void shouldReturnNullWhenGetEncounterAnError() throws Exception {

        String datasetId = "43874232515345";
        File metadataFile = new File(storeLocation + '/' + datasetId);

        try {
            // given
            repository.add(getMetadata(datasetId));

            // when

            FileOutputStream fos = new FileOutputStream(metadataFile);
            fos.write("invalid content in dataset metadata file".getBytes());
            fos.close();

            final DataSetMetadata actual = repository.get(datasetId);

            // then
            assertNull(actual);
        } finally {
            metadataFile.delete();
        }
    }

    @Test
    public void shouldUpdateExistingEntry() throws IOException {

        String id = "75396";

        // given
        final DataSetMetadata metadata = getMetadata(id);
        repository.add(metadata);

        // when
        DataSetMetadata update = builder.build().readerFor(DataSetMetadata.class)
                .readValue(this.getClass().getResourceAsStream("dataset_2.json"));
        update = DataSetMetadata.Builder.metadata().copy(update).id(id).build();

        repository.add(update);

        // then
        final DataSetMetadata actual = repository.get(id);
        assertEquals(update, actual);
    }

    @Test
    public void shouldComputeSizeList() throws IOException {

        // no metadata
        assertEquals(0, repository.size());

        // 14 metadata
        int expected = 14;
        for (int i = 1; i <= expected; i++) {
            repository.add(getMetadata(String.valueOf(i)));
        }

        assertEquals(expected, repository.size());

        // 14-5 = 9 metadata
        int removed = 5;
        for (int i = 1; i <= removed; i++) {
            repository.remove(String.valueOf(i));
        }

        assertEquals(expected - removed, repository.size());
    }

    @Test
    public void shouldRemove() throws IOException {

        String id = "735145";

        // first try to remove a non existing metadata to make sure it does not break anything
        repository.remove(id);

        // given
        repository.add(getMetadata(id));

        // when
        repository.remove(id);

        // then
        assertNull(repository.get(id));
    }

    @Test
    public void shouldList() throws IOException {

        // list nothing
        final Iterable<DataSetMetadata> emptyList = repository.list();
        assertFalse(emptyList.iterator().hasNext());

        // given
        int expected = 26;
        for (int i = 1; i <= expected; i++) {
            repository.add(getMetadata(String.valueOf(i)));
        }

        // when
        final Iterable<DataSetMetadata> actual = repository.list();

        // then
        final AtomicInteger count = new AtomicInteger(0); // need of a final object that can be incremented in the
                                                          // following lambda expression
        actual.forEach(dataSetMetadata -> {
            assertTrue(Integer.valueOf(dataSetMetadata.getId()) <= expected);
            count.addAndGet(1);
        });

        assertEquals(expected, count.intValue());
    }

    @Test
    public void shouldClear() throws IOException {

        // given
        assertEquals(0, repository.size());

        int count = 11;
        for (int i = 1; i <= count; i++) {
            repository.add(getMetadata(String.valueOf(i)));
        }
        assertEquals(count, repository.size());

        // when
        repository.clear();
        assertEquals(0, repository.size());
    }

    @Test
    public void shouldIgnoreHiddenFiles() throws Exception {

        // given
        assertFalse(repository.list().iterator().hasNext());

        // when
        File hidden = new File("target/test/store/metadata/.hidden_file");
        FileOutputStream fos = new FileOutputStream(hidden);
        fos.write("hello".getBytes());

        // then
        final DataSetMetadata dataSetMetadata = repository.get(".hidden");
        hidden.delete();
        assertNull(dataSetMetadata);

    }

    /**
     * Return a dataset metadata with the given id.
     * 
     * @param id the wanted dataset id.
     * @return a dataset metadata with the given id.
     * @throws IOException if an error occurs reading the json source file.
     */
    public DataSetMetadata getMetadata(String id) throws IOException {
        DataSetMetadata original = builder.build().readerFor(DataSetMetadata.class)
                .readValue(this.getClass().getResourceAsStream("dataset.json"));
        return DataSetMetadata.Builder.metadata().copy(original).id(id).build();
    }

}