import { takeEvery } from 'redux-saga/effects';
import { OPEN_WINDOW, REDIRECT_WINDOW } from '../../constants/actions';
import windowSagas from './window.saga';

const url = 'http://url.fake';

const action = {
	payload: {
		url,
	},
};

describe('Window', () => {
	describe('open', () => {
		beforeEach(() => {
			global.window.open = jest.fn();
		});

		it('should register saga', () => {
			const gen = windowSagas.open();

			expect(gen.next().value)
				.toEqual(takeEvery(OPEN_WINDOW, windowSagas.openWindow));
		});

		it('should open new window', () => {
			windowSagas.openWindow(action);

			expect(global.window.open)
				.toHaveBeenCalledWith(url, '_blank');
		});
	});

	describe('redirect', () => {
		let location;

		beforeEach(() => {
			location = global.window.location.assign;
			global.window.open = jest.fn();
			global.window.location.assign = jest.fn();
		});

		afterEach(() => {
			global.window.location.assign = location;
		});

		it('should register saga', () => {
			const gen = windowSagas.redirect();

			expect(gen.next().value)
				.toEqual(takeEvery(REDIRECT_WINDOW, windowSagas.redirectWindow));
		});

		it('should redirect same window', () => {
			windowSagas.redirectWindow(action);

			expect(global.window.location.assign)
				.toHaveBeenCalledWith(url);
		});
	});
});
