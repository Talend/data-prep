import { takeEvery } from 'redux-saga/effects';
import { OPEN_WINDOW, REDIRECT_WINDOW } from '../../../constants/actions';
import { default as sagas } from '../redirect.saga';
import * as effects from '../../effects/redirect.effects';


describe('redirect', () => {
	describe('open', () => {
		it('should register saga', () => {
			const gen = sagas['window:open']();
			expect(gen.next().value).toEqual(takeEvery(OPEN_WINDOW, effects.open));
		});
	});

	describe('redirect', () => {
		it('should register saga', () => {
			const gen = sagas['window:redirect']();
			expect(gen.next().value).toEqual(takeEvery(REDIRECT_WINDOW, effects.redirect));
		});
	});
});
