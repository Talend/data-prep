import { delay } from 'redux-saga';
import { call, cancel, fork, take, takeLatest, all, put, select } from 'redux-saga/effects';
import http from '@talend/react-cmf/lib/sagas/http';
import { actions } from '@talend/react-cmf';
import { Typeahead } from '@talend/react-containers';
import { SEARCH, SEARCH_SELECT, SEARCH_RESET, OPEN_WINDOW } from '../constants/actions';
import { default as creators } from '../actions';
import {
	DEFAULT_SEARCH_PAYLOAD,
	DEBOUNCE_TIMEOUT,
	DOCUMENTATION_SEARCH_URL,
	DATAPREP_SEARCH_URL,

	SEARCH_CATEGORIES_BY_PROVIDER,
} from '../constants/search';

import SearchService from '../services/search.service';


// function* process(payload) {
// 	yield delay(DEBOUNCE_TIMEOUT);
// 	const [tdp, doc] = yield all([call(dataprep, payload), call(documentation, payload)]);
// 	const categories = tdp.categories;
//
// 	const results = [...adaptTDPResults(tdp), ...adaptSearchResult(doc)];
// 	const items = categories
// 		.filter(({ type }) => results.some(result => result.inventoryType === type))
// 		.map((inventory) => {
// 			const suggestions = results.filter(result => result.inventoryType === inventory.type);
// 			let label = inventory.type;
// 			if (categories) {
// 				label = categories.find(category => category.type === inventory.type).label;
// 			}
//
// 			return {
// 				title: label,
// 				icon: {
// 					name: 'fixme', // inventory.iconName,
// 					title: label,
// 				},
// 				suggestions,
// 			};
// 		});
// 	yield put(Typeahead.setStateAction({ searching: false }, 'headerbar:search'));
// 	// yield put(actions.components.mergeState('Container(Typeahead)', 'headerbar:search', { searching: false }));
// 	yield put(actions.collections.addOrReplace('search', items));
// }
//
// function* search() {
// 	let task;
// 	while (true) {
// 		const { payload } = yield take(SEARCH);
//
// 		if (task) {
// 			yield cancel(task);
// 		}
// 		// const searchState = select(Typeahead.getState)
// 		yield put(Typeahead.setStateAction({ searching: true }, 'headerbar:search'));
// 		// yield put(actions.components.mergeState('Container(Typeahead)', 'headerbar:search', { searching: true }));
// 		task = yield fork(process, payload);
// 	}
// }
//
// function* reset() {
// 	yield takeLatest(SEARCH_RESET, function* () {
// 		yield put(actions.collections.addOrReplace('search', null));
// 	});
// }


function* goTo() {
	while (true) {
		const { payload } = yield take(SEARCH_SELECT);
		const results = yield select(state => state.cmf.collections.get('search'));
		const item = results
			.get(payload.sectionIndex)
			.get('suggestions')
			.get(payload.itemIndex)
			.toJS();

		switch (item.inventoryType) {
		case 'preparation':
			yield put(creators.preparation.open(null, { type: 'preparation', id: item.id }));
			break;
		case 'dataset':
			// yield put();
			break;
		case 'folder':
			yield put(creators.folder.open(null, { id: item.id }));
			break;
		case 'documentation':
			yield put({
				type: OPEN_WINDOW,
				payload: { url: item.url },
			});
		}
	}
}

// function* documentation(query) {
// 	const { data } = yield call(http.post, DOCUMENTATION_SEARCH_URL, {
// 		...DEFAULT_SEARCH_PAYLOAD,
// 		query,
// 	});
//
// 	return data;
// }
//
// function* dataprep(query) {
// 	const { data } = yield call(http.get, `${DATAPREP_SEARCH_URL}${query}`);
// 	return JSON.parse(data);
// }

function* reset() {
	yield takeLatest(SEARCH_RESET, function* () {
		yield put(actions.collections.addOrReplace('search', null));
	});
}

function* process(payload) {
	yield delay(DEBOUNCE_TIMEOUT);
	const test = Object.keys(SEARCH_CATEGORIES_BY_PROVIDER).map(
		provider => SearchService.build(
			provider,
			SEARCH_CATEGORIES_BY_PROVIDER[provider],
			payload,
		)
	);
	console.log('[NC] test: ', test);
	console.log('[NC] test[0]: ', test[0]);

	// const res = yield all(test.map(t1 => all(t1.map(put))));

	const res = yield all(test.map((t1) => {
		console.log('[NC] t1: ', t1);

		return (all(t1.map((t2) => {
			console.log('[NC] t2: ', t2);
			return call(...t2);
		})));
	}));

	console.log('[NC] res: ', res);


	// console.log('[NC] SearchService -> test: ', test);
	// const [tdp, doc] = yield all([call(dataprep, payload), call(documentation, payload)]);
	// const categories = tdp.categories;
	//
	// const results = [...adaptTDPResults(tdp), ...adaptSearchResult(doc)];
	// const items = categories
	// 	.filter(({ type }) => results.some(result => result.inventoryType === type))
	// 	.map((inventory) => {
	// 		const suggestions = results.filter(result => result.inventoryType === inventory.type);
	// 		let label = inventory.type;
	// 		if (categories) {
	// 			label = categories.find(category => category.type === inventory.type).label;
	// 		}
	//
	// 		return {
	// 			title: label,
	// 			icon: {
	// 				name: 'fixme', // inventory.iconName,
	// 				title: label,
	// 			},
	// 			suggestions,
	// 		};
	// 	});
	// yield put(Typeahead.setStateAction({ searching: false }, 'headerbar:search'));
	// // yield put(actions.components.mergeState('Container(Typeahead)', 'headerbar:search', { searching: false }));
	// yield put(actions.collections.addOrReplace('search', items));
}

function* search() {
	let task;
	while (true) {
		const { payload } = yield take(SEARCH);

		if (task) {
			yield cancel(task);
		}

		yield put(Typeahead.setStateAction({ searching: true }, 'headerbar:search'));
		task = yield fork(process, payload);
	}
}

function adaptTDPResults(data) {
	let items = [];
	const mapping = [
		{
			key: 'datasets',
			inventory: 'dataset',
		},
		{
			key: 'preparations',
			inventory: 'preparation',
		},
		{
			key: 'folders',
			inventory: 'folder',
		},
	];

	mapping.forEach((type) => {
		data[type.key].forEach((item) => {
			item.inventoryType = type.inventory;
			item.tooltipName = item.name;
			// FIXME [NC]:
			item.title = item.name;
			// itemToDisplay.model = item;
		});

		items = items.concat(data[type.key]);
	});

	return items;
}

function adaptSearchResult(data) {
	const normalize = (str) => {
		const dom = document.createElement('p');
		dom.innerHTML = str.replace(/(<[^>]*>)/g, '');
		return dom.innerText;
	};

	return data.results.map(topic => ({
		inventoryType: 'documentation',
		description: normalize(topic.htmlExcerpt),
		title: normalize(topic.htmlTitle),
		url: topic.occurrences[0].readerUrl,
	}));
}

export default {
	search,
	reset,
	goTo,
};
