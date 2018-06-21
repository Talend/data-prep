import actions from '../search';


describe('Search action', () => {
	it('should create a start action', () => {
		const event = {
			target: {
				value: 'test',
			},
		};
		const action = actions.start(event);

		expect(action).toMatchSnapshot();
	});

	it('should create a select action', () => {
		const event = {};
		const payload = {
			sectionIndex: 42,
			itemIndex: 666,
		};
		const action = actions.select(event, payload);

		expect(action).toMatchSnapshot();
	});

	it('should create a select action', () => {
		const action = actions.reset();

		expect(action).toMatchSnapshot();
	});
});
