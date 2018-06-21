import actions from '../dataset';


describe('Dataset action', () => {
	it('should create an open action', () => {
		const event = {};
		const payload = {
			type: 'test',
			id: 42,
		};
		const action = actions.open(event, payload);

		expect(action).toMatchSnapshot();
	});
});
