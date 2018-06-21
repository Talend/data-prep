import actions from '../folder';


describe('Folder action', () => {
	it('should create an open action', () => {
		const event = {};
		const payload = {
			id: 42,
		};
		const action = actions.open(event, payload);

		expect(action).toMatchSnapshot();
	});
});
