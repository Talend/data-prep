import React from 'react';
import Immutable from 'immutable';
import { shallow } from 'enzyme';
import { AboutModal } from './AboutModal.component';

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
		const state = new Immutable.Map({ show: true, expanded: false });
		const wrapper = shallow(
			<AboutModal
				services={SERVICES}
				state={state}
				displayVersion="mock version"
				t={k => k}
			/>,
		);
		expect(wrapper.getElement()).toMatchSnapshot();
	});

	it('should render in expanded mode', () => {
		const state = new Immutable.Map({ show: true, expanded: true });
		const wrapper = shallow(
			<AboutModal
				services={SERVICES}
				state={state}
				displayVersion="mock version"
				t={k => k}
			/>,
		);
		expect(wrapper.getElement()).toMatchSnapshot();
	});
});
