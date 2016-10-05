/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import PreparationBreadcrumbController from './preparation-breadcrumb-controller';

const PreparationBreadcrumbComponent = {
	template: `
        <breadcrumb items="$ctrl.state.inventory.breadcrumb"
                    children="$ctrl.state.inventory.breadcrumbChildren"
                    on-select="$ctrl.go(item)"
                    on-list-open="$ctrl.fetchChildren(item)"></breadcrumb>
    `,
	controller: PreparationBreadcrumbController,
};

export default PreparationBreadcrumbComponent;
