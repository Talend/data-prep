

/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const HomePreparationComponent = {
	template: '<div class="home-main"><preparation-header ng-if="!$ctrl.state.inventory.isFetchingPreparations" class="inventory-list-header"></preparation-header>' +
        '<preparation-list class="inventory-list-container"></preparation-list></div>',
	controller(state) {
		'ngInject';
		this.state = state;
	},
};
export default HomePreparationComponent;
