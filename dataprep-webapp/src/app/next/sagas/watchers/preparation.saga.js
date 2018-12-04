import { all, call, take } from 'redux-saga/effects';
import {
	OPEN_WITH_BUTTON_CLICKED,
} from '@talend/dataset/lib/app/components/SampleView/DatasetDetailSubHeaderActions/DatasetDetailSubHeaderActions.constants';

import * as actions from '../../constants/actions';
import * as effects from '../effects/preparation.effects';

function* fetch() {
	while (true) {
		const { payload } = yield take(actions.FETCH_PREPARATIONS);
		yield call(effects.refresh, payload);
	}
}

function* create() {
	while (true) {
		const { payload } = yield take(actions.CREATE_PREPARATIONS);
		yield call(effects.create, payload);
	}
}

function* openWith() {
	while (true) {
		const result = yield take(OPEN_WITH_BUTTON_CLICKED);
		yield call(effects.create, result.payload || result);
	}
}

function* openPreparationCreatorModal() {
	while (true) {
		yield take(actions.OPEN_PREPARATION_CREATOR);
		yield call(effects.openPreparationCreatorModal);
	}
}

export default {
	'preparation:create': create,
	'preparation:openWith': openWith,
	'preparation:fetch': fetch,
	'preparation:creator:open': openPreparationCreatorModal,
};
