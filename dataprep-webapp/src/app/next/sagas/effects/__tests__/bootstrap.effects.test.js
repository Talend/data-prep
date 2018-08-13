import store from 'store';
import { sagaRouter } from '@talend/react-cmf';
import { browserHistory as history } from 'react-router';
import { fork } from 'redux-saga/effects';
import * as effects from '../../effects/bootstrap.effects';
import { refresh } from '../preparation.effects';


describe('bootstrap', () => {
	describe('fetch', () => {
		it('should update cmf store', () => {
			store.set('settings', { actions: [], uris: { preparation: 'api/preparation' } });
			const gen = effects.fetch();
			const expected = store.get('settings');
			expect(gen.next().value).toEqual(expected);
			let effect = gen.next(expected).value.PUT.action;
			expect(effect.type).toBe('REACT_CMF.COLLECTION_ADD_OR_REPLACE');
			expect(effect.collectionId).toBe('settings');
			expect(effect.data).toEqual(expected);

			const route = {
				'/preparations': { saga: refresh, runOnExactMatch: true },
				'/preparations/:folderId': { saga: refresh, runOnExactMatch: true },
			};
			effect = gen.next().value;
			expect(effect).toEqual(fork(sagaRouter, history, route));
			expect(gen.next().done).toBeTruthy();
		});
	});
});
