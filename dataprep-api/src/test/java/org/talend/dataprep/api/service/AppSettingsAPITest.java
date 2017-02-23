package org.talend.dataprep.api.service;

import static com.jayway.restassured.RestAssured.when;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.PAYLOAD_ARGS_KEY;
import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.PAYLOAD_METHOD_KEY;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.talend.dataprep.api.service.settings.AppSettings;
import org.talend.dataprep.api.service.settings.actions.api.ActionDropdownSettings;
import org.talend.dataprep.api.service.settings.actions.api.ActionSettings;
import org.talend.dataprep.api.service.settings.actions.api.ActionSplitDropdownSettings;
import org.talend.dataprep.api.service.settings.views.api.appheaderbar.AppHeaderBarSettings;
import org.talend.dataprep.api.service.settings.views.api.breadcrumb.BreadcrumbSettings;
import org.talend.dataprep.api.service.settings.views.api.list.ListSettings;
import org.talend.dataprep.api.service.settings.views.api.sidepanel.SidePanelSettings;

public class AppSettingsAPITest extends ApiServiceTestBase {

    @Test
    public void shouldCreateActionsSettings() throws Exception {
        // when
        final AppSettings settings = when().get("/api/settings/").as(AppSettings.class);

        // then
        final ActionSettings datasetClone = settings.getActions().get("dataset:clone");
        assertThat(datasetClone.getName(), is("Copy dataset"));
        assertThat(datasetClone.getIcon(), is("talend-files-o"));
        assertThat(datasetClone.getType(), is("@@dataset/CLONE"));
        assertThat(datasetClone.getPayload().get(PAYLOAD_METHOD_KEY), is("clone"));

        final ActionSettings datasetCreate = settings.getActions().get("dataset:create");
        assertThat(datasetCreate.getName(), is("Add Dataset"));
        assertThat(datasetCreate.getIcon(), is("talend-plus-circle"));
        assertThat(datasetCreate.getType(), is("@@dataset/CREATE"));
        assertThat(datasetCreate.getBsStyle(), is("primary"));

        final ActionSettings datasetDisplayMode = settings.getActions().get("dataset:display-mode");
        assertThat(datasetDisplayMode.getName(), is("Change dataset display mode"));
        assertThat(datasetDisplayMode.getType(), is("@@inventory/DISPLAY_MODE"));
        assertThat(datasetDisplayMode.getPayload().get(PAYLOAD_METHOD_KEY), is("setDatasetsDisplayMode"));

        final ActionSettings datasetFavorite = settings.getActions().get("dataset:favorite");
        assertThat(datasetFavorite.getName(), is("Add dataset in your favorite list"));
        assertThat(datasetFavorite.getIcon(), is("talend-star"));
        assertThat(datasetFavorite.getType(), is("@@dataset/FAVORITE"));
        assertThat(datasetFavorite.getPayload().get(PAYLOAD_METHOD_KEY), is("toggleFavorite"));

        final ActionSettings datasetFetch = settings.getActions().get("datasets:fetch");
        assertThat(datasetFetch.getName(), is("Fetch all datasets"));
        assertThat(datasetFetch.getType(), is("@@dataset/DATASET_FETCH"));

        final ActionSettings datasetOpen = settings.getActions().get("dataset:open");
        assertThat(datasetOpen.getName(), is("Open dataset"));
        assertThat(datasetOpen.getIcon(), is("talend-datastore"));
        assertThat(datasetOpen.getType(), is("@@dataset/OPEN"));

        final ActionSettings datasetRemove = settings.getActions().get("dataset:remove");
        assertThat(datasetRemove.getName(), is("Remove dataset"));
        assertThat(datasetRemove.getIcon(), is("talend-trash"));
        assertThat(datasetRemove.getType(), is("@@dataset/REMOVE"));

        final ActionSettings datasetSort = settings.getActions().get("dataset:sort");
        assertThat(datasetSort.getName(), is("Change dataset sort"));
        assertThat(datasetSort.getType(), is("@@dataset/SORT"));
        assertThat(datasetSort.getPayload().get(PAYLOAD_METHOD_KEY), is("changeSort"));

        final ActionSettings datasetSubmitEdit = settings.getActions().get("dataset:submit-edit");
        assertThat(datasetSubmitEdit.getName(), is("Submit name edition"));
        assertThat(datasetSubmitEdit.getType(), is("@@dataset/SUBMIT_EDIT"));

        final ActionSettings datasetUpdate = settings.getActions().get("dataset:update");
        assertThat(datasetUpdate.getName(), is("Update dataset"));
        assertThat(datasetUpdate.getIcon(), is("talend-file-move"));
        assertThat(datasetUpdate.getType(), is("@@dataset/UPDATE"));

        final ActionSettings externalDocumentation = settings.getActions().get("external:documentation");
        assertThat(externalDocumentation.getName(), is("Documentation"));
        assertThat(externalDocumentation.getIcon(), is("talend-question-circle"));
        assertThat(externalDocumentation.getType(), is("@@external/OPEN_WINDOW"));
        assertThat(externalDocumentation.getPayload().get(PAYLOAD_METHOD_KEY), is("open"));

        final ActionSettings externalHelp = settings.getActions().get("external:help");
        assertThat(externalHelp.getName(), is("Open Online Help"));
        assertThat(externalHelp.getIcon(), is("talend-question-circle"));
        assertThat(externalHelp.getType(), is("@@external/OPEN_WINDOW"));
        assertThat(externalHelp.getPayload().get(PAYLOAD_METHOD_KEY), is("open"));
        assertThat(((List<String>) externalHelp.getPayload().get(PAYLOAD_ARGS_KEY)).get(0),
                is("https://help.talend.com/pages/viewpage.action?pageId=266307043&utm_medium=dpdesktop&utm_source=header"));

        final ActionSettings inventoryCancelEdit = settings.getActions().get("inventory:cancel-edit");
        assertThat(inventoryCancelEdit.getName(), is("Cancel name edition"));
        assertThat(inventoryCancelEdit.getType(), is("@@inventory/CANCEL_EDIT"));
        assertThat(inventoryCancelEdit.getPayload().get(PAYLOAD_METHOD_KEY), is("disableInventoryEdit"));

        final ActionSettings inventoryEdit = settings.getActions().get("inventory:edit");
        assertThat(inventoryEdit.getName(), is("Edit name"));
        assertThat(inventoryEdit.getIcon(), is("talend-pencil"));
        assertThat(inventoryEdit.getType(), is("@@inventory/EDIT"));
        assertThat(inventoryEdit.getPayload().get(PAYLOAD_METHOD_KEY), is("enableInventoryEdit"));

        final ActionSettings menuDatasets = settings.getActions().get("menu:datasets");
        assertThat(menuDatasets.getName(), is("Datasets"));
        assertThat(menuDatasets.getIcon(), is("talend-datastore"));
        assertThat(menuDatasets.getType(), is("@@router/GO"));
        assertThat(menuDatasets.getPayload().get(PAYLOAD_METHOD_KEY), is("go"));
        assertThat(((List<String>) menuDatasets.getPayload().get(PAYLOAD_ARGS_KEY)).get(0), is("home.datasets"));

        final ActionSettings menuFolders = settings.getActions().get("menu:folders");
        assertThat(menuFolders.getName(), is("Folders"));
        assertThat(menuFolders.getIcon(), is("talend-folder"));
        assertThat(menuFolders.getType(), is("@@router/GO_FOLDER"));
        assertThat(menuFolders.getPayload().get(PAYLOAD_METHOD_KEY), is("go"));
        assertThat(((List<String>) menuFolders.getPayload().get(PAYLOAD_ARGS_KEY)).get(0), is("home.preparations"));

        final ActionSettings menuPlaygroundDataset = settings.getActions().get("menu:playground:dataset");
        assertThat(menuPlaygroundDataset.getName(), is("Create new preparation"));
        assertThat(menuPlaygroundDataset.getIcon(), is("talend-datastore"));
        assertThat(menuPlaygroundDataset.getType(), is("@@router/GO_DATASET"));
        assertThat(menuPlaygroundDataset.getPayload().get(PAYLOAD_METHOD_KEY), is("go"));
        assertThat(((List<String>) menuPlaygroundDataset.getPayload().get(PAYLOAD_ARGS_KEY)).get(0), is("playground.dataset"));

        final ActionSettings menuPlaygroundPreparation = settings.getActions().get("menu:playground:preparation");
        assertThat(menuPlaygroundPreparation.getName(), is("Open Preparation"));
        assertThat(menuPlaygroundPreparation.getIcon(), is("talend-dataprep"));
        assertThat(menuPlaygroundPreparation.getType(), is("@@router/GO_PREPARATION"));
        assertThat(menuPlaygroundPreparation.getPayload().get(PAYLOAD_METHOD_KEY), is("go"));
        assertThat(((List<String>) menuPlaygroundPreparation.getPayload().get(PAYLOAD_ARGS_KEY)).get(0),
                is("playground.preparation"));

        final ActionSettings menuPreparations = settings.getActions().get("menu:preparations");
        assertThat(menuPreparations.getName(), is("Preparations"));
        assertThat(menuPreparations.getIcon(), is("talend-dataprep"));
        assertThat(menuPreparations.getType(), is("@@router/GO_CURRENT_FOLDER"));
        assertThat(menuPreparations.getPayload().get(PAYLOAD_METHOD_KEY), is("go"));
        assertThat(((List<String>) menuPreparations.getPayload().get(PAYLOAD_ARGS_KEY)).get(0), is("home.preparations"));

        final ActionSettings modalAbout = settings.getActions().get("modal:about");
        assertThat(modalAbout.getName(), is("About Data Preparation"));
        assertThat(modalAbout.getIcon(), is("talend-info-circle"));
        assertThat(modalAbout.getType(), is("@@modal/SHOW"));
        assertThat(modalAbout.getPayload().get(PAYLOAD_METHOD_KEY), is("toggleAbout"));

        final ActionSettings modalFeedback = settings.getActions().get("modal:feedback");
        assertThat(modalFeedback.getName(), is("Send feedback to Talend"));
        assertThat(modalFeedback.getIcon(), is("talend-bubbles"));
        assertThat(modalFeedback.getType(), is("@@modal/SHOW"));
        assertThat(modalFeedback.getPayload().get(PAYLOAD_METHOD_KEY), is("showFeedback"));

        final ActionDropdownSettings listDatasetPreparations = (ActionDropdownSettings) settings.getActions()
                .get("list:dataset:preparations");
        assertThat(listDatasetPreparations.getName(), is("Open Preparation"));
        assertThat(listDatasetPreparations.getIcon(), is("talend-dataprep"));
        assertThat(listDatasetPreparations.getItems(), is("preparations"));
        assertThat(listDatasetPreparations.getDynamicAction(), is("menu:playground:preparation"));
        assertThat(listDatasetPreparations.getStaticActions().get(0), is("menu:playground:dataset"));

        final ActionSettings onboardingPreparation = settings.getActions().get("onboarding:preparation");
        assertThat(onboardingPreparation.getName(), is("Click here to discover the application"));
        assertThat(onboardingPreparation.getIcon(), is("talend-board"));
        assertThat(onboardingPreparation.getType(), is("@@onboarding/START_TOUR"));
        assertThat(onboardingPreparation.getPayload().get(PAYLOAD_METHOD_KEY), is("startTour"));
        assertThat(((List<String>) onboardingPreparation.getPayload().get(PAYLOAD_ARGS_KEY)).get(0), is("preparation"));

        final ActionSettings preparationCopyMove = settings.getActions().get("preparation:copy-move");
        assertThat(preparationCopyMove.getName(), is("Copy/Move preparation"));
        assertThat(preparationCopyMove.getIcon(), is("talend-files-o"));
        assertThat(preparationCopyMove.getType(), is("@@preparation/COPY_MOVE"));

        final ActionSettings preparationCreate = settings.getActions().get("preparation:create");
        assertThat(preparationCreate.getName(), is("Create preparation"));
        assertThat(preparationCreate.getIcon(), is("talend-plus-circle"));
        assertThat(preparationCreate.getType(), is("@@preparation/CREATE"));
        assertThat(preparationCreate.getBsStyle(), is("primary"));
        assertThat(preparationCreate.getPayload().get(PAYLOAD_METHOD_KEY), is("togglePreparationCreator"));

        final ActionSettings preparationDisplayMode = settings.getActions().get("preparation:display-mode");
        assertThat(preparationDisplayMode.getName(), is("Change preparation display mode"));
        assertThat(preparationDisplayMode.getType(), is("@@inventory/DISPLAY_MODE"));
        assertThat(preparationDisplayMode.getPayload().get(PAYLOAD_METHOD_KEY), is("setPreparationsDisplayMode"));

        final ActionSettings preparationFolderCreate = settings.getActions().get("preparation:folder:create");
        assertThat(preparationFolderCreate.getName(), is("Create folder"));
        assertThat(preparationFolderCreate.getIcon(), is("talend-folder"));
        assertThat(preparationFolderCreate.getType(), is("@@preparation/CREATE"));
        assertThat(preparationFolderCreate.getPayload().get(PAYLOAD_METHOD_KEY), is("toggleFolderCreator"));

        final ActionSettings preparationFolderFetch = settings.getActions().get("preparations:folder:fetch");
        assertThat(preparationFolderFetch.getName(), is("Fetch preparations from current folder"));
        assertThat(preparationFolderFetch.getIcon(), is("talend-dataprep"));
        assertThat(preparationFolderFetch.getType(), is("@@preparation/FOLDER_FETCH"));

        final ActionSettings preparationFolderRemove = settings.getActions().get("preparation:folder:remove");
        assertThat(preparationFolderRemove.getName(), is("Remove folder"));
        assertThat(preparationFolderRemove.getIcon(), is("talend-trash"));
        assertThat(preparationFolderRemove.getType(), is("@@preparation/FOLDER_REMOVE"));
        assertThat(preparationFolderRemove.getPayload().get(PAYLOAD_METHOD_KEY), is("remove"));

        final ActionSettings preparationRemove = settings.getActions().get("preparation:remove");
        assertThat(preparationRemove.getName(), is("Remove preparation"));
        assertThat(preparationRemove.getIcon(), is("talend-trash"));
        assertThat(preparationRemove.getType(), is("@@preparation/REMOVE"));

        final ActionSettings preparationSort = settings.getActions().get("preparation:sort");
        assertThat(preparationSort.getName(), is("Change preparation sort"));
        assertThat(preparationSort.getType(), is("@@preparation/SORT"));
        assertThat(preparationSort.getPayload().get(PAYLOAD_METHOD_KEY), is("changeSort"));

        final ActionSettings preparationSubmitEdit = settings.getActions().get("preparation:submit-edit");
        assertThat(preparationSubmitEdit.getName(), is("Submit name edition"));
        assertThat(preparationSubmitEdit.getType(), is("@@preparation/SUBMIT_EDIT"));

        final ActionSettings searchAll = settings.getActions().get("search:all");
        assertThat(searchAll.getType(), is("@@search/ALL"));

        final ActionSettings searchFocus = settings.getActions().get("search:focus");
        assertThat(searchFocus.getType(), is("@@search/FOCUS"));

        final ActionSettings searchToggle = settings.getActions().get("search:toggle");
        assertThat(searchToggle.getName(), is("Toggle search input"));
        assertThat(searchToggle.getIcon(), is("talend-search"));
        assertThat(searchToggle.getType(), is("@@search/TOGGLE"));

        final ActionSettings sidepanelToggle = settings.getActions().get("sidepanel:toggle");
        assertThat(sidepanelToggle.getName(), is("Click here to toggle the side panel"));
        assertThat(sidepanelToggle.getIcon(), is("talend-arrow-left"));
        assertThat(sidepanelToggle.getType(), is("@@sidepanel/TOGGLE"));
        assertThat(sidepanelToggle.getPayload().get(PAYLOAD_METHOD_KEY), is("toggleHomeSidepanel"));
    }

    @Test
    public void shouldCreateAppHeaderBarViewSettings() throws Exception {
        // when
        final AppSettings settings = when().get("/api/settings/").as(AppSettings.class);

        // then
        final AppHeaderBarSettings ahb = (AppHeaderBarSettings) settings.getViews().get("appheaderbar");
        assertThat(ahb.getApp(), is("Data Preparation"));
        assertThat(ahb.getBrandLink().getTitle(), is("Talend Data Preparation"));
        assertThat(ahb.getBrandLink().getOnClick(), is("menu:preparations"));
        assertThat(ahb.getSearch().getDebounceTimeout(), is(300));
        assertThat(ahb.getSearch().getPlaceholder(), is("Search Talend Data Preparation and Documentation"));
        assertThat(ahb.getSearch().getOnBlur(), is("search:toggle"));
        assertThat(ahb.getSearch().getOnChange(), is("search:all"));
        assertThat(ahb.getSearch().getOnKeyDown(), is("search:focus"));
        assertThat(ahb.getSearch().getOnToggle(), is("search:toggle"));
        assertThat(ahb.getSearch().getOnSelect().get("folder"), is("menu:folders"));
        assertThat(ahb.getSearch().getOnSelect().get("documentation"), is("external:documentation"));
        assertThat(ahb.getSearch().getOnSelect().get("dataset"), is("dataset:open"));
        assertThat(ahb.getSearch().getOnSelect().get("preparation"), is("menu:playground:preparation"));
        assertThat(ahb.getActions(), contains("onboarding:preparation", "modal:feedback", "external:help", "modal:about"));
    }

    @Test
    public void shouldCreateBreadcrumbSettings() throws Exception {
        // when
        final AppSettings settings = when().get("/api/settings/").as(AppSettings.class);

        // then
        final BreadcrumbSettings breadcrumb = (BreadcrumbSettings) settings.getViews().get("breadcrumb");
        assertThat(breadcrumb.getMaxItems(), is(5));
        assertThat(breadcrumb.getOnItemClick(), is("menu:folders"));
    }

    @Test
    public void shouldCreateSidePanelSettings() throws Exception {
        // when
        final AppSettings settings = when().get("/api/settings/").as(AppSettings.class);

        // then
        final SidePanelSettings sidePanel = (SidePanelSettings) settings.getViews().get("sidepanel");
        assertThat(sidePanel.getOnToggleDock(), is("sidepanel:toggle"));
        assertThat(sidePanel.getActions(), contains("menu:preparations", "menu:datasets"));
    }

    @Test
    public void shouldCreateFolderListSettings() throws Exception {
        // when
        final AppSettings settings = when().get("/api/settings/").as(AppSettings.class);

        // then
        final ListSettings list = (ListSettings) settings.getViews().get("listview:folders");
        assertThat(list.getList().getTitleProps().getOnClick(), is("menu:folders"));
    }

    @Test
    public void shouldCreatePreparationListSettings() throws Exception {
        // when
        final AppSettings settings = when().get("/api/settings/").as(AppSettings.class);

        // then
        final ListSettings list = (ListSettings) settings.getViews().get("listview:preparations");
        assertThat(list.getDidMountActionCreator(), is("preparations:folder:fetch"));

        final List<String> keys = map(list.getList().getColumns(), "key");
        final List<String> labels = map(list.getList().getColumns(), "label");
        assertThat(keys, contains("name", "author", "creationDate", "lastModificationDate", "datasetName", "nbSteps"));
        assertThat(labels, contains("Name", "Author", "Created", "Modified", "Dataset", "Steps"));
        assertThat(list.getList().getItemProps().getClassNameKey(), is("className"));
        assertThat(list.getList().getTitleProps().getIconKey(), is("icon"));
        assertThat(list.getList().getTitleProps().getKey(), is("name"));
        assertThat(list.getList().getTitleProps().getOnClick(), is("menu:playground:preparation"));
        assertThat(list.getList().getTitleProps().getOnEditCancel(), is("inventory:cancel-edit"));
        assertThat(list.getList().getTitleProps().getOnEditSubmit(), is("preparation:submit-edit"));

        assertThat(list.getToolbar().getActionBar().getActions().get("left"),
                contains("preparation:create", "preparation:folder:create"));
    }

    @Test
    public void shouldCreateDatasetListSettings() throws Exception {
        // when
        final AppSettings settings = when().get("/api/settings/").as(AppSettings.class);

        // then
        final ListSettings list = (ListSettings) settings.getViews().get("listview:datasets");
        assertThat(list.getDidMountActionCreator(), is("datasets:fetch"));

        final List<String> keys = map(list.getList().getColumns(), "key");
        final List<String> labels = map(list.getList().getColumns(), "label");
        final List<String> types = map(list.getList().getColumns(), "type");
        assertThat(keys, contains("name", "statusActions", "author", "creationDate", "nbRecords"));
        assertThat(labels, contains("Name", "", "Author", "Created", "Rows"));
        assertThat(types, contains(null, "actions", null, null, null));
        assertThat(list.getList().getItemProps().getClassNameKey(), is("className"));
        assertThat(list.getList().getTitleProps().getIconKey(), is("icon"));
        assertThat(list.getList().getTitleProps().getKey(), is("name"));
        assertThat(list.getList().getTitleProps().getOnClick(), is("dataset:open"));
        assertThat(list.getList().getTitleProps().getOnEditCancel(), is("inventory:cancel-edit"));
        assertThat(list.getList().getTitleProps().getOnEditSubmit(), is("dataset:submit-edit"));

        assertThat(list.getToolbar().getActionBar().getActions().get("left"), contains("dataset:create"));
    }

    @Test
    public void shouldInsertImportTypesInDatasetCreateDropdown() throws Exception {
        // when
        final AppSettings settings = when().get("/api/settings/").as(AppSettings.class);

        // then
        final ActionSplitDropdownSettings datasetCreate = (ActionSplitDropdownSettings) settings.getActions()
                .get("dataset:create");
        final List<Object> importTypes = datasetCreate.getItems();

        final Map<String, Object> localImport = (Map<String, Object>) importTypes.get(0);
        assertThat(localImport.get("locationType"), is("local"));
        assertThat(localImport.get("contentType"), is("text/plain"));
        assertThat(localImport.get("dynamic"), is(false));
        assertThat(localImport.get("defaultImport"), is(true));
        assertThat(localImport.get("label"), is("Local file"));
        assertThat(localImport.get("title"), is("Add local file dataset"));

        final Map<String, Object> hdfsImport = (Map<String, Object>) importTypes.get(1);
        assertThat(hdfsImport.get("locationType"), is("hdfs"));
        assertThat(hdfsImport.get("contentType"), is("application/vnd.remote-ds.hdfs"));
        assertThat(hdfsImport.get("dynamic"), is(false));
        assertThat(hdfsImport.get("defaultImport"), is(false));
        assertThat(hdfsImport.get("label"), is("From HDFS"));
        assertThat(hdfsImport.get("title"), is("Add HDFS dataset"));

        final Map<String, Object> httpImport = (Map<String, Object>) importTypes.get(2);
        assertThat(httpImport.get("locationType"), is("http"));
        assertThat(httpImport.get("contentType"), is("application/vnd.remote-ds.http"));
        assertThat(httpImport.get("dynamic"), is(false));
        assertThat(httpImport.get("defaultImport"), is(false));
        assertThat(httpImport.get("label"), is("From HTTP"));
        assertThat(httpImport.get("title"), is("Add HTTP dataset"));
    }

    private List<String> map(final List<Map<String, String>> list, final String property) {
        return list.stream().map(col -> col.get(property)).collect(toList());
    }
}
