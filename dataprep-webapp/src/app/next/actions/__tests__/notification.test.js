import actions from '../notification';

describe('Notification action', () => {
	describe('success', () => {
		it('should create an success notification action', () => {
			const event = {};
			const payload = {
				title: 'success',
				message: 'message',
			};
			const action = actions.success(event, payload);

			expect(action).toMatchSnapshot();
		});
	});
	describe('error', () => {
		it('should create an error notification action', () => {
			const event = {};
			const payload = {
				title: 'error',
				message: 'message',
			};
			const action = actions.error(event, payload);

			expect(action).toMatchSnapshot();
		});
	});
	describe('warning', () => {
		it('should create an warning notification action', () => {
			const event = {};
			const payload = {
				title: 'warning',
				message: 'message',
			};
			const action = actions.warning(event, payload);

			expect(action).toMatchSnapshot();
		});
	});
});
