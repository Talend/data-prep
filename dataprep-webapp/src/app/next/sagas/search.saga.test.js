import SagaTester from 'redux-saga-tester';
import sagas from './search.saga';

import { SEARCH_RESET, SEARCH_SELECT } from '../constants/actions';
import { INITIAL_STATE } from './search.saga.mock';


describe('Search', () => {
	describe('reset', () => {
		it('should reset search state', () => {
			const tester = new SagaTester({
				initialState: {},
			});
			tester.start(() => sagas.reset());

			tester.dispatch({
				type: SEARCH_RESET,
			});

			const actions = tester.getCalledActions();
			expect(actions[actions.length - 1]).toEqual({
				type: 'REACT_CMF.COLLECTION_ADD_OR_REPLACE',
				collectionId: 'search',
				data: null,
			});
		});
	});


	describe('goto', () => {
		beforeEach(() => {
			global.window.open = jest.fn();
		});

		it('should redirect to the appropriate content', () => {
			const tester = new SagaTester({
				initialState: INITIAL_STATE,
			});
			tester.start(() => sagas.goto());

			tester.dispatch({
				type: SEARCH_SELECT,
				payload: {
					sectionIndex: 0,
					itemIndex: 2,
				},
			});

			const actions = tester.getCalledActions();
			console.log('[NC] actions: ', actions);
			// expect(actions[actions.length - 1]).toEqual({
			// 	type: 'REACT_CMF.COLLECTION_ADD_OR_REPLACE',
			// 	collectionId: 'search',
			// 	data: null,
			// });
		});
	});
});
