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

package org.talend.dataprep.api.service.settings;

import static java.util.Arrays.stream;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.service.settings.actions.api.ActionSettings;
import org.talend.dataprep.api.service.settings.context.api.ContextSettings;
import org.talend.dataprep.api.service.settings.analytics.api.AnalyticsSettings;
import org.talend.dataprep.api.service.settings.help.api.HelpSettings;
import org.talend.dataprep.api.service.settings.uris.api.UriSettings;
import org.talend.dataprep.api.service.settings.views.api.ViewSettings;

/**
 * App settings service
 */
@Service
public class AppSettingsService {

    @Autowired
    private AppSettingsProvider<ActionSettings>[] actionsProviders;

    @Autowired(required = false)
    private AppSettingsConfigurer<ActionSettings>[] actionsConfigurers;

    @Autowired
    private AppSettingsProvider<ViewSettings>[] viewsProviders;

    @Autowired(required = false)
    private AppSettingsConfigurer<ViewSettings>[] viewsConfigurers;

    @Autowired
    private AppSettingsProvider<UriSettings>[] urisProviders;

    @Autowired(required = false)
    private AppSettingsConfigurer<UriSettings>[] urisConfigurers;

    @Autowired
    private AppSettingsProvider<HelpSettings>[] helpProviders;

    @Autowired(required = false)
    private AppSettingsConfigurer<HelpSettings>[] helpConfigurers;

    @Autowired
    private AppSettingsProvider<ContextSettings>[] contextProviders;

    @Autowired(required = false)
    private AppSettingsConfigurer<ContextSettings>[] contextConfigurers;

    @Autowired
    private AppSettingsProvider<AnalyticsSettings>[] analyticsProviders;

    @Autowired(required = false)
    private AppSettingsConfigurer<AnalyticsSettings>[] analyticsConfigurers;

    public AppSettingsConfigurer<ActionSettings>[] getActionsConfigurers() {
        return actionsConfigurers;
    }

    public void setActionsConfigurers(AppSettingsConfigurer<ActionSettings>... actionsConfigurers) {
        this.actionsConfigurers = actionsConfigurers;
    }

    /**
     * Generate the app settings
     */
    public AppSettings getSettings() {
        final AppSettings appSettings = new AppSettings();

        // populate appSettings actions dictionary (key: actionId, value: action)
        getSettingsStream(actionsProviders, actionsConfigurers) //
                .filter(ActionSettings::isEnabled)
                .map(ActionSettings::translate)
                .forEach(action -> appSettings.getActions().put(action.getId(), action));

        // populate appSettings views dictionary (key: viewId, value: view)
        getSettingsStream(viewsProviders, viewsConfigurers) //
                .map(ViewSettings::translate)
                .forEach(view -> appSettings.getViews().put(view.getId(), view));

        // populate appSettings uris (key: uriName, value: uriValue)
        getSettingsStream(urisProviders, urisConfigurers) //
                .forEach(uri -> appSettings.getUris().put(uri.getId(), uri.getUri()));

        // populate appSettings documentation (key: documentationProperty, value: documentationValue)
        getSettingsStream(helpProviders, helpConfigurers) //
                .forEach(help -> appSettings.getHelp().put(help.getId(), help.getValue()));

        // populate appSettings context (key: contextProperty, value: contextValue)
        getSettingsStream(contextProviders, contextConfigurers) //
                .forEach(context -> appSettings.getContext().put(context.getId(), context.getValue()));

        // populate appSettings analytics
        getSettingsStream(analyticsProviders, analyticsConfigurers) //
                .forEach(analytics -> appSettings.getAnalytics().put(analytics.getId(), analytics.getValue()));

        return appSettings;
    }

    /**
     * Build a Setting mapper Function from a given AppSettingsConfigurer
     *
     * @param configurer The setting configurer
     * @return The function
     */
    private <T> Function<T, T> configure(final AppSettingsConfigurer<T> configurer) {
        return action -> configurer.isApplicable(action) && configurer.isUserAuthorized() ? configurer.configure(action) : action;
    }

    /**
     * Get all the static settings as a stream
     *
     * @return Stream that will process all static settings
     */
    private <T> Stream<T> getStaticSettingsStream(final AppSettingsProvider<T>[] providers) {
        return stream(providers).map(AppSettingsProvider::getSettings).flatMap(Collection::stream);
    }

    /**
     * Get all the configured settings as a stream
     *
     * @param providers The array of settings providers
     * @param configurers The array of settings configurers
     * @param <T> ActionSettings | ViewSettings
     * @return The stream of configured settings
     */
    private <T> Stream<T> getSettingsStream(final AppSettingsProvider<T>[] providers,
            final AppSettingsConfigurer<T>[] configurers) {
        // build a stream
        // * from static actions
        // * each action goes through all the actions configurers
        Stream<T> settingsStream = getStaticSettingsStream(providers);
        if (configurers != null) {
            for (final AppSettingsConfigurer<T> configurer : configurers) {
                settingsStream = settingsStream //
                        .map(configure(configurer)); //
            }
        }
        return settingsStream;
    }
}
