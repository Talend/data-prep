import React from 'react';
import Immutable from 'immutable';
import { shallow } from 'enzyme';
import Container from './AboutModal.component';
import Connected from './AboutModal.connect';

const SERVICES = Immutable.fromJS([
	{
		service: 'streams inventory',
		build_id: '029b0427bf3a9fd83cee0c830af911485db5dc28',
		version_id: '0.7.0-SNAPSHOT',
	},
	{
		service: 'streams runner',
		build_id: '029b0427bf3a9fd83cee0c830af911485db5dc28',
		version_id: '0.7.0-SNAPSHOT',
	},
	{
		service: 'preview runner',
		build_id: '029b0427bf3a9fd83cee0c830af911485db5dc28',
		version_id: '0.7.0-SNAPSHOT',
	},
	{
		service: 'dataset',
		build_id: '029b0427bf3a9fd83cee0c830af911485db5dc28',
		version_id: '0.7.0 - SNAPSHOT',
	},
	{
		service: 'websocket relay',
		build_id: '029b0427bf3a9fd83cee0c830af911485db5dc28',
		version_id: '0.7.0-SNAPSHOT',
	},
]);

describe('Container(AboutModal)', () => {
	it('should render', () => {
		const state = new Immutable.Map({ show: true, expanded: true });
		const wrapper = shallow(
			<Container.WrappedComponent
				services={SERVICES}
				state={state}
				displayVersion="mock version"
			/>,
		);
		expect(wrapper.getElement()).toMatchSnapshot();
	});

	// it('should onClick Less call setState with expanded to false', () => {
	// 	const state = new Immutable.Map({ show: true, expanded: true });
	// 	const setState = jest.fn();
	// 	const wrapper = shallow(
	// 		<Container.WrappedComponent
	// 			services={SERVICES}
	// 			state={state}
	// 			setState={setState}
	// 			copyright="Talend. All right reserved"
	// 			version="Winter is coming"
	// 		/>
	// 	);
	// 	wrapper.find(Action).simulate('click');
	// 	expect(setState).toHaveBeenCalled();
	// 	expect(setState.mock.calls[0][0].toJS()).toEqual({
	// 		expanded: false,
	// 	});
	// });
	//
	// it('should onClick More call setState with expanded to true', () => {
	// 	const state = new Immutable.Map({ show: true, expanded: false });
	// 	const setState = jest.fn();
	// 	const wrapper = shallow(
	// 		<Container.WrappedComponent
	// 			services={SERVICES}
	// 			state={state}
	// 			setState={setState}
	// 			copyright="Talend. All right reserved"
	// 			version="Winter is coming"
	// 		/>
	// 	);
	// 	wrapper.find(Action).simulate('click');
	// 	expect(setState).toHaveBeenCalled();
	// 	expect(setState.mock.calls[0][0].toJS()).toEqual({
	// 		expanded: true,
	// 	});
	// });
});
