import { takeLatest } from 'redux-saga/effects';
import sagas from '../http.saga';
import * as effects from '../../effects/http.effects';


describe('http', () => {
	describe('open', () => {
		it('should wait for HTTP errors actions', () => {
			const gen = sagas['http:errors:handler']();

			expect(gen.next().value).toEqual(takeLatest('@@HTTP/ERRORS', effects.handle));
		});
	});
});
