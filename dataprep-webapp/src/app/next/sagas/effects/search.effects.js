import { call, all, put, select } from 'redux-saga/effects';
import api, { actions } from '@talend/react-cmf';
import { Typeahead } from '@talend/react-containers';
import { OPEN_WINDOW } from '../../constants/actions';
import { default as creators } from '../../actions';
import SearchService from '../../services/search.service';


export function* goto(payload) {
	const results = yield select(state => state.cmf.collections.get('search'));
	const item = results
		.get(payload.sectionIndex)
		.get('suggestions')
		.get(payload.itemIndex)
		.toJS();

	switch (item.type) {
	case 'preparation':
	case 'dataset':
	case 'folder':
		yield put(creators[item.type].open(null, item));
		break;
	case 'documentation':
		yield put({
			type: OPEN_WINDOW,
			payload: { url: item.url },
		});
	}
}

export function* reset() {
	yield put(actions.collections.addOrReplace('search', null));
}

export function* search(payload) {
	yield put(Typeahead.setStateAction({ searching: true }, 'headerbar:search'));

	const categories = api.registry.getFromRegistry('SEARCH_CATEGORIES_BY_PROVIDER');
	const providers = Object.keys(categories);
	const service = new SearchService(categories);
	const batches = providers.map(
		provider => service.build(
			provider,
			payload,
		)
	);
	const results = (
			yield all(batches.map(request => call(...request)))
		).map(
			(result, index) => service.transform(providers[index], result)
		);

	yield put(Typeahead.setStateAction({ searching: false }, 'headerbar:search'));
	yield put(actions.collections.addOrReplace(
		'search',
		[].concat(...results).filter(s => s.suggestions.length),
	));
}
