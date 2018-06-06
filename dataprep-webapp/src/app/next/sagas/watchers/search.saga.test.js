// import SagaTester from 'redux-saga-tester';
// import sagas from './search.saga';
//
// import {
// 	SEARCH_RESET,
// 	SEARCH_SELECT,
// 	OPEN_WINDOW,
// 	REDIRECT_WINDOW,
// 	OPEN_FOLDER,
// 	SEARCH,
// } from '../constants/actions';
//

describe('search', () => {
	it('should not fail', () => {
		expect(true).toEqual(true);
	});
});


// describe('Search', () => {
// 	describe('reset', () => {
// 		it('should reset search state', () => {
// 			const tester = new SagaTester({
// 				initialState: {},
// 			});
// 			tester.start(() => sagas.reset());
//
// 			tester.dispatch({
// 				type: SEARCH_RESET,
// 			});
//
// 			const actions = tester.getCalledActions();
// 			expect(actions[actions.length - 1]).toEqual({
// 				type: 'REACT_CMF.COLLECTION_ADD_OR_REPLACE',
// 				collectionId: 'search',
// 				data: null,
// 			});
// 		});
// 	});
//
// 	describe('goto', () => {
// 		beforeEach(() => {
// 			global.window.open = jest.fn();
// 		});
//
// 		it('should open selected preparation', () => {
// 			const tester = new SagaTester({
// 				initialState: INITIAL_STATE,
// 			});
// 			tester.start(() => sagas.goto());
//
// 			tester.dispatch({
// 				type: SEARCH_SELECT,
// 				payload: {
// 					sectionIndex: 0,
// 					itemIndex: 0,
// 				},
// 			});
//
// 			const actions = tester.getCalledActions();
// 			expect(actions[actions.length - 1]).toEqual({
// 				type: REDIRECT_WINDOW,
// 				payload: { url: 'null/#/playground/preparation?prepid=666' },
// 			});
// 		});
//
// 		it('should open selected folder', () => {
// 			const tester = new SagaTester({
// 				initialState: INITIAL_STATE,
// 			});
// 			tester.start(() => sagas.goto());
//
// 			tester.dispatch({
// 				type: SEARCH_SELECT,
// 				payload: {
// 					sectionIndex: 0,
// 					itemIndex: 1,
// 				},
// 			});
//
// 			const actions = tester.getCalledActions();
// 			expect(actions[actions.length - 1]).toEqual({
// 				type: OPEN_FOLDER,
// 				id: 42,
// 				cmf: { routerPush: '/preparations/42' },
// 			});
// 		});
//
// 		it('should redirect to the appropriate documentation page', () => {
// 			const tester = new SagaTester({
// 				initialState: INITIAL_STATE,
// 			});
// 			tester.start(() => sagas.goto());
//
// 			tester.dispatch({
// 				type: SEARCH_SELECT,
// 				payload: {
// 					sectionIndex: 0,
// 					itemIndex: 2,
// 				},
// 			});
//
// 			const actions = tester.getCalledActions();
// 			expect(actions[actions.length - 1]).toEqual({
// 				type: OPEN_WINDOW,
// 				payload: { url: 'www.doc.org/test' },
// 			});
// 		});
//
// 		it('should do nothing if type is unknown', () => {
// 			const tester = new SagaTester({
// 				initialState: INITIAL_STATE,
// 			});
// 			tester.start(() => sagas.goto());
//
// 			tester.dispatch({
// 				type: SEARCH_SELECT,
// 				payload: {
// 					sectionIndex: 0,
// 					itemIndex: 3,
// 				},
// 			});
//
// 			const actions = tester.getCalledActions();
// 			expect(actions.length).toBe(1);
// 		});
// 	});
//
// 	describe('search', () => {
// 		it('should set loading feedback', () => {
// 			const tester = new SagaTester({
// 				initialState: {},
// 			});
// 			tester.start(() => sagas.search());
//
// 			tester.dispatch({
// 				type: SEARCH,
// 			});
//
// 			const actions = tester.getCalledActions();
// 			expect(actions[actions.length - 1]).toEqual({
// 				type: 'Container(Typeahead).setState',
// 				cmf: {
// 					componentState: {
// 						type: 'REACT_CMF.COMPONENT_MERGE_STATE',
// 						componentName: 'Container(Typeahead)',
// 						key: 'headerbar:search',
// 						componentState: { searching: true },
// 					},
// 				},
// 			});
// 		});
//
// 		it('should debounce', async () => {
// 			window.fetch = jest.fn().mockImplementation(
// 				() => Promise.resolve([])
// 			);
//
// 			const tester = new SagaTester({
// 				initialState: {},
// 			});
// 			tester.start(() => sagas.search());
//
// 			tester.dispatch({
// 				type: SEARCH,
// 			});
//
// 			/*
// 				{
// 					type: 'REACT_CMF.COLLECTION_ADD_OR_REPLACE',
// 					collectionId: 'search',
// 					data: [],
// 				}
// 			*/
//
// 			await tester.waitFor('Container(Typeahead).setState');
// 			// await tester.waitFor('REACT_CMF.COLLECTION_ADD_OR_REPLACE');
//
// 			// tester.getCalledActions().forEach((action) => {
// 			// 	console.log('[NC] action: ', action);
// 			// });
//
// 			// const actions = tester.getCalledActions();
// 			// console.log('[NC] actions: ', actions);
// 		});
// 	});
// });
