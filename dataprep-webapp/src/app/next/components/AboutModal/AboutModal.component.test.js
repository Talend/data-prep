import React from 'react';
import Immutable from 'immutable';
import { shallow } from 'enzyme';
import { AboutModal } from './AboutModal.component';

const SERVICES = Immutable.fromJS(
	['API', 'Dataset', 'Preparation', 'Transformation'].map(serviceName => ({
		versionId: '2.8.0-SNAPSHOT',
		buildId: '87d0dcd-12e0d6f',
		serviceName,
	}))
);

describe('Container(AboutModal)', () => {
	it('should render', () => {
		const state = new Immutable.Map({ show: true, expanded: false });
		const wrapper = shallow(
			<AboutModal services={SERVICES} state={state} displayVersion="mock version" />,
		);
		expect(wrapper.getElement()).toMatchSnapshot();
	});

	it('should render in expanded mode', () => {
		const state = new Immutable.Map({ show: true, expanded: true });
		const wrapper = shallow(
			<AboutModal services={SERVICES} state={state} displayVersion="mock version" />,
		);
		expect(wrapper.getElement()).toMatchSnapshot();
	});
});
