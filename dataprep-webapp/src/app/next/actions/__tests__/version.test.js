import actions from '../version';

describe('Version action', () => {
	it('should create a fetch action', () => {
		const action = actions.fetch();

		expect(action).toMatchSnapshot();
	});
});
