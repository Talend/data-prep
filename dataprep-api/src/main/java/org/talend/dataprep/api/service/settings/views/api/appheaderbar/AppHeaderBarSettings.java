// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import org.talend.dataprep.api.service.settings.views.api.ViewSettings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * An app header bar is a static bar placed on the top of the window
 * see
 * https://talend.github.io/react-talend-components/?selectedKind=App%20Header%20Bar&selectedStory=default&full=0&down=1&left=1&panelRight=0&downPanel=kadirahq%2Fstorybook-addon-actions%2Factions-panel
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
    private LinkSettings logo;

    /**
     * The brand link configuration
     */
    private LinkSettings brand;

    /**
     * The search bar configuration
     */
    private SearchSettings search;

    /**
     * The help configuration
     */
    private String help;

    /**
     * The user dropdown action
     */
    private String userMenu;

    /**
     * The products dropdown action
     */
    private String products;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ViewSettings translate() {
        return AppHeaderBarSettings //
                .from(this) //
                .translate() //
                .build();
    }

    public void setId(String id) {
        this.id = id;
    }

    public LinkSettings getLogo() {
        return logo;
    }

    public void setLogo(LinkSettings logo) {
        this.logo = logo;
    }

    public LinkSettings getBrand() {
        return brand;
    }

    public void setBrand(LinkSettings brand) {
        this.brand = brand;
    }

    public SearchSettings getSearch() {
        return search;
    }

    public void setSearch(SearchSettings search) {
        this.search = search;
    }

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public String getUserMenu() {
        return userMenu;
    }

    public void setUserMenu(String userMenu) {
        this.userMenu = userMenu;
    }

    public String getProducts() {
        return products;
    }

    public void setProducts(String products) {
        this.products = products;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder from(final AppHeaderBarSettings viewSettings) {
        return builder() //
                .id(viewSettings.getId()) //
                .logo(viewSettings.getLogo()) //
                .brand(viewSettings.getBrand()) //
                .search(viewSettings.getSearch()) //
                .help(viewSettings.getHelp()) //
                .userMenu(viewSettings.getUserMenu()) //
                .products(viewSettings.getProducts());
    }

    public static class Builder {

        private String id;

        private LinkSettings logo;

        private LinkSettings brand;

        private SearchSettings search;

        private String help;

        private String userMenu;

        private String products;

        public Builder id(final String id) {
            this.id = id;
            return this;
        }

        public Builder logo(final LinkSettings logo) {
            this.logo = logo;
            return this;
        }

        public Builder brand(final LinkSettings brand) {
            this.brand = brand;
            return this;
        }

        public Builder search(final SearchSettings search) {
            this.search = search;
            return this;
        }

        public Builder help(final String help) {
            this.help = help;
            return this;
        }

        public Builder removeHelp() {
            this.help = null;
            return this;
        }

        public Builder userMenu(final String userMenu) {
            this.userMenu = userMenu;
            return this;
        }

        public Builder products(final String products) {
            this.products = products;
            return this;
        }

        public Builder translate() {
            if (this.logo != null) {
                this.logo = LinkSettings.from(this.logo).translate().build();
            }
            if (this.brand != null) {
                this.brand = LinkSettings.from(this.brand).translate().build();
            }
            if (this.search != null) {
                this.search = SearchSettings.from(this.search).translate().build();
            }
            return this;
        }

        public AppHeaderBarSettings build() {
            final AppHeaderBarSettings settings = new AppHeaderBarSettings();
            settings.setId(this.id);
            settings.setLogo(this.logo);
            settings.setBrand(this.brand);
            settings.setSearch(this.search);
            settings.setHelp(this.help);
            settings.setUserMenu(this.userMenu);
            settings.setProducts(this.products);
            return settings;
        }
    }
}
