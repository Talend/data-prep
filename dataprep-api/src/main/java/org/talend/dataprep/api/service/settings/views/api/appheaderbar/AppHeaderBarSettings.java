// ============================================================================
//
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

package org.talend.dataprep.api.service.settings.views.api.appheaderbar;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.ArrayList;
import java.util.List;

import org.talend.dataprep.api.service.settings.views.api.ViewSettings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * An app header bar is a static bar placed on the top of the window
 * see https://talend.github.io/react-talend-components/?selectedKind=App%20Header%20Bar&selectedStory=default&full=0&down=1&left=1&panelRight=0&downPanel=kadirahq%2Fstorybook-addon-actions%2Factions-panel
 */
@JsonInclude(NON_NULL)
public class AppHeaderBarSettings implements ViewSettings {

    public static final String VIEW_TYPE = TYPE_APP_HEADER_BAR;

    /**
     * The id that is the key to the view dictionary
     */
    @JsonIgnore
    private String id;

    /**
     * The app name
     */
    private String app;

    /**
     * The brand link configuration
     */
    private LinkSettings brandLink;

    /**
     * The search bar configuration
     */
    private SearchSettings search;

    /**
     * The list of actions to display as a list of icons
     */
    private List<String> actions;

    /**
     * The user dropdown action
     */
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
        return builder() //
                .id(viewSettings.getId()) //
                .app(viewSettings.getApp()) //
                .brandLink(viewSettings.getBrandLink()) //
                .search(viewSettings.getSearch()) //
                .actions(viewSettings.getActions()) //
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
