import * as effects from './redirect.effects';

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

		it('should open new window', () => {
			effects.open(action);

			expect(global.window.open).toHaveBeenCalledWith(url, '_blank');
		});
	});

	describe('redirect', () => {
		let location;

		beforeEach(() => {
			location = global.window.location.assign;
			global.window.location.assign = jest.fn();
		});

		afterEach(() => {
			global.window.location.assign = location;
		});

		it('should redirect same window', () => {
			effects.redirect(action);

			expect(global.window.location.assign).toHaveBeenCalledWith(url);
		});
	});
});
