import actions from '../help';


describe('Help action', () => {
	it('should create an openAbout action', () => {
		const action = actions.openAbout();

		expect(action).toMatchSnapshot();
	});
});
