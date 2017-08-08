/*  ============================================================================

 Copyright (C) 2006-2017 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export const TYPES = {
	PROGRESSION: 'PROGRESSION',
	INFINITE: 'INFINITE',
};

export const STATES = {
	IN_PROGRESS: 'IN_PROGRESS',
	FUTURE: 'FUTURE',
	COMPLETE: 'COMPLETE',
};

export const DEFAULTS = {
	DATASET: [
		{
			type: TYPES.PROGRESSION,
			state: STATES.IN_PROGRESS,
			label: 'UPLOADING_FILE',
		},
		{
			type: TYPES.INFINITE,
			state: STATES.FUTURE,
			label: 'PROFILING_DATA',
		},
	],
};
