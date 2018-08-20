import store from 'store';
import { sagaRouter } from '@talend/react-cmf';
import { browserHistory as history } from 'react-router';
import { call, fork } from 'redux-saga/effects';
import * as effects from '../../effects/bootstrap.effects';
import { refresh } from '../preparation.effects';


describe('bootstrap', () => {
	describe('bootstrap', () => {
		it('should call fetchSettings and initializeRouter', () => {
			const gen = effects.bootstrap();
			expect(gen.next().value).toEqual(call(effects.fetchSettings));
			expect(gen.next().value).toEqual(call(effects.initializeRouter));
		});
	});
	describe('fetchSettings', () => {
		it('should update cmf store', () => {
			store.set('settings', { actions: [], uris: { preparation: 'api/preparation' } });
			const gen = effects.fetchSettings();
			const expected = store.get('settings');
			expect(gen.next().value).toEqual(expected);
			const effect = gen.next(expected).value.PUT.action;
			expect(effect.type).toBe('REACT_CMF.COLLECTION_ADD_OR_REPLACE');
			expect(effect.collectionId).toBe('settings');
			expect(effect.data).toEqual(expected);
		});
	});

	describe('initializeRouter', () => {
		it('should init sagaRouter', () => {
			const gen = effects.initializeRouter();
			const route = {
				'/preparations': { saga: refresh, runOnExactMatch: true },
				'/preparations/:folderId': { saga: refresh, runOnExactMatch: true },
			};
			const effect = gen.next().value;
			expect(effect).toEqual(fork(sagaRouter, history, route));
			expect(gen.next().done).toBeTruthy();
		});
	});
});
