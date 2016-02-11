package org.talend.dataprep.api.service.upgrade;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.service.ApiServiceTestBase;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpgradeServerVersionTest extends ApiServiceTestBase {

    @Autowired
    ObjectMapper mapper;

    @Test
    public void singleVersion() throws Exception {
        List<UpgradeServerVersion> versions = mapper.readerFor(new TypeReference<List<UpgradeServerVersion>>() {
        }).readValue(UpgradeServerVersionTest.class.getResourceAsStream("upgrade_server_1.json"));
        assertThat(versions.size(), CoreMatchers.is(1));
    }

    @Test
    public void singleVersionContent() throws Exception {
        List<UpgradeServerVersion> versions = mapper.readerFor(new TypeReference<List<UpgradeServerVersion>>() {
        }).readValue(UpgradeServerVersionTest.class.getResourceAsStream("upgrade_server_1.json"));
        assertThat(versions.size(), CoreMatchers.is(1));
        final UpgradeServerVersion upgradeServerVersion = versions.get(0);
        assertThat(upgradeServerVersion.getVersion(), CoreMatchers.is("1.0.1"));
        assertThat(upgradeServerVersion.getTitle(), CoreMatchers.is("Bug fixes & stability"));
        assertThat(upgradeServerVersion.getDownloadUrl(), CoreMatchers.is("http://blabla.com/download/1.0.1"));
        assertThat(upgradeServerVersion.getReleaseNoteUrl(), CoreMatchers.is("http://blabla.com/download/releases"));
    }

    @Test
    public void multipleVersions() throws Exception {
        List<UpgradeServerVersion> versions = mapper.readerFor(new TypeReference<List<UpgradeServerVersion>>() {
        }).readValue(UpgradeServerVersionTest.class.getResourceAsStream("upgrade_server_2.json"));
        assertThat(versions.size(), CoreMatchers.is(2));
    }
}