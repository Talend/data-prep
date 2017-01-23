package org.talend.dataprep.api.service.upgrade;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpgradeServerVersion {
    @JsonProperty("version")
    public String version;

    @JsonProperty("title")
    public String title;

    @JsonProperty("download_url")
    public String downloadUrl;

    @JsonProperty("release_note_url")
    public String releaseNoteUrl;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getReleaseNoteUrl() {
        return releaseNoteUrl;
    }

    public void setReleaseNoteUrl(String releaseNoteUrl) {
        this.releaseNoteUrl = releaseNoteUrl;
    }

    @Override
    @SuppressWarnings("ControlFlowStatementWithoutBraces")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpgradeServerVersion that = (UpgradeServerVersion) o;
        return Objects.equals(version, that.version) && Objects.equals(title, that.title) && Objects.equals(downloadUrl,
                that.downloadUrl) && Objects.equals(releaseNoteUrl, that.releaseNoteUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, title, downloadUrl, releaseNoteUrl);
    }
}
