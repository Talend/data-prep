import { delay } from 'redux-saga';
import { createMockTask } from 'redux-saga/utils';
import { take, call, fork, takeLatest, cancel } from 'redux-saga/effects';
import sagas from '../search.saga';
import * as effects from '../../effects/search.effects';
import { SEARCH_SELECT, SEARCH_RESET, SEARCH } from '../../../constants/actions';
import { DEBOUNCE_TIMEOUT } from '../../../constants/search';


describe('Search', () => {
	describe('reset', () => {
		it('should handle reset action and call the appropriate effect', () => {
			const gen = sagas['search:reset']();
			expect(gen.next().value).toEqual(takeLatest(SEARCH_RESET, effects.reset));
		});
	});

	describe('goto', () => {
		const action = {
			payload: { type: 'folder' },
		};

		it('should handle search select action and call the appropriate effect', () => {
			const gen = sagas['search:goto']();
			expect(gen.next().value).toEqual(take(SEARCH_SELECT));
			expect(gen.next(action).value).toEqual(call(effects.goto, action.payload));
		});
	});

	describe('search', () => {
		const action = {
			payload: { term: 'test' },
		};

		it('should handle search action and call the appropriate effect', () => {
			const gen = sagas['search:process']();
			const task = createMockTask();
			expect(gen.next().value).toEqual(take(SEARCH));

			// now we need to go inside the forked generator,
			// so we retrieve it from the action :
			const leaf = gen.next(action).value;
			const cb = leaf.FORK.fn;
			const forked = cb(action.payload);

			expect(leaf).toEqual(fork(cb, action.payload));

			// inside the forked generator :
			expect(forked.next().value).toEqual(call(delay, DEBOUNCE_TIMEOUT));
			expect(forked.next().value).toEqual(call(effects.search, action.payload));

			// in parallel, we wanna check that the debounce works properly
			// if it is, the task must be cancelled at the next iteration :
			expect(gen.next(task).value).toEqual(take(SEARCH));
			expect(gen.next(action).value).toEqual(cancel(task));
		});
	});
});
