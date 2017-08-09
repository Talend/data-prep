/*  ============================================================================

 Copyright (C) 2006-2017 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.progress.service:ProgressConstants
 * @description The REST api services url
 */
export default function ProgressConstants() {
	this.TYPES = {
		PROGRESSION: 'PROGRESSION',
		INFINITE: 'INFINITE',
	};

	this.STATES = {
		IN_PROGRESS: 'IN_PROGRESS',
		FUTURE: 'FUTURE',
		COMPLETE: 'COMPLETE',
	};

	this.SCHEMAS = {
		DATASET: {
			title: 'ADD_NEW_DATASET',
			steps: [
				{
					type: this.TYPES.PROGRESSION,
					state: this.STATES.IN_PROGRESS,
					label: 'UPLOADING_FILE',
				},
				{
					type: this.TYPES.INFINITE,
					state: this.STATES.FUTURE,
					label: 'PROFILING_DATA',
				},
			],
		},
	};
}
