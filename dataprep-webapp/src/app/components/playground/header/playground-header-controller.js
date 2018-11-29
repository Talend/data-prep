/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/


import { PLAYGROUND_PREPARATION_ROUTE } from '../../../index-route';

export default class PlaygroundHeaderCtrl {
	constructor($state, $translate, state, StateService, PlaygroundService) {
		'ngInject';

		this.$state = $state;
		this.$translate = $translate;
		this.state = state;
		this.StateService = StateService;
		this.PlaygroundService = PlaygroundService;

		this.setNameInEditMode = this.setNameInEditMode.bind(this);
		this.cancelNameInEditMode = this.cancelNameInEditMode.bind(this);
		this.editName = this.editName.bind(this);
	}

	setNameInEditMode() {
		this.StateService.setNameEditionMode(true);
	}

	cancelNameInEditMode() {
		this.StateService.setNameEditionMode(false);
	}

	editName(event, { value }) {
		const cleanName = value.trim();
		if (!this.loading && cleanName) {
			this.loading = true;
			this.PlaygroundService.createOrUpdatePreparation(cleanName)
				.then(({ id }) => {
					this.$state.go(PLAYGROUND_PREPARATION_ROUTE, { prepid: id });
				})
				.finally(() => {
					this.StateService.setNameEditionMode(false);
					this.loading = false;
				});
		}
	}

	get subtitle() {
		const { nameEditionMode, dataset } = this.state.playground;
		const { instant } = this.$translate;
		return nameEditionMode ? '' : `${instant('DATASET')}${instant('COLON')} ${dataset.name}`;
	}

	get title() {
		const { preparation, dataset } = this.state.playground;
		return (preparation || dataset).name;
	}
}
