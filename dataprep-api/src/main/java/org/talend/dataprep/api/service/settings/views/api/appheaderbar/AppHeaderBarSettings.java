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

package org.talend.dataprep.api.service.settings.views.api.appheaderbar;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.talend.dataprep.api.service.settings.views.api.ViewSettings;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public class AppHeaderBarSettings implements ViewSettings {
    public static final String VIEW_TYPE = "AppHeaderBar";

    @JsonIgnore
    private String id;
    private String app;
    private LinkSettings brandLink;
    private SearchSettings search;
    private List<String> actions;
    private String userMenu;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public LinkSettings getBrandLink() {
        return brandLink;
    }

    public void setBrandLink(LinkSettings brandLink) {
        this.brandLink = brandLink;
    }

    public SearchSettings getSearch() {
        return search;
    }

    public void setSearch(SearchSettings search) {
        this.search = search;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public String getUserMenu() {
        return userMenu;
    }

    public void setUserMenu(String userMenu) {
        this.userMenu = userMenu;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder from(final AppHeaderBarSettings viewSettings) {
        return builder()
                .id(viewSettings.getId())
                .app(viewSettings.getApp())
                .brandLink(viewSettings.getBrandLink())
                .search(viewSettings.getSearch())
                .actions(viewSettings.getActions())
                .userMenu(viewSettings.getUserMenu());
    }

    public static class Builder {
        private String id;
        private String app;
        private LinkSettings brandLink;
        private SearchSettings search;
        private List<String> actions = new ArrayList<>();
        private String userMenu;

        public Builder id(final String id) {
            this.id = id;
            return this;
        }

        public Builder app(final String app) {
            this.app = app;
            return this;
        }

        public Builder brandLink(final LinkSettings brandLink) {
            this.brandLink = brandLink;
            return this;
        }

        public Builder search(final SearchSettings search) {
            this.search = search;
            return this;
        }

        public Builder action(final String action) {
            this.actions.add(action);
            return this;
        }

        public Builder actions(final List<String> actions) {
            this.actions.addAll(actions);
            return this;
        }

        public Builder clearActions() {
            this.actions.clear();
            return this;
        }

        public Builder userMenu(final String userMenu) {
            this.userMenu = userMenu;
            return this;
        }

        public AppHeaderBarSettings build() {
            final AppHeaderBarSettings settings = new AppHeaderBarSettings();
            settings.setId(this.id);
            settings.setApp(this.app);
            settings.setBrandLink(this.brandLink);
            settings.setSearch(this.search);
            settings.setActions(this.actions);
            settings.setUserMenu(this.userMenu);
            return settings;
        }

    }
}
