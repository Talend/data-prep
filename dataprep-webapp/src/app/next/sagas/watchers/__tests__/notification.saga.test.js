import { takeEvery } from 'redux-saga/effects';
import sagas from '../notification.saga';
import * as effects from '../../effects/notification.effects';
import { ERROR_NOTIFICATION, SUCCESS_NOTIFICATION, WARNING_NOTIFICATION } from '../../../constants/actions';

describe('notification', () => {
	describe('success', () => {
		it('should wait for success notification', () => {
			const gen = sagas['notification:success']();

			expect(gen.next().value).toEqual(takeEvery(SUCCESS_NOTIFICATION, effects.success));
		});
	});
	describe('error', () => {
		it('should wait for error notification', () => {
			const gen = sagas['notification:error']();

			expect(gen.next().value).toEqual(takeEvery(ERROR_NOTIFICATION, effects.error));
		});
	});
	describe('warning', () => {
		it('should wait for warning notification', () => {
			const gen = sagas['notification:warning']();

			expect(gen.next().value).toEqual(takeEvery(WARNING_NOTIFICATION, effects.warning));
		});
	});
});
