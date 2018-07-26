import React from 'react';
import { cmfConnect } from '@talend/react-cmf';
import { Map } from 'immutable';
import { Breadcrumbs } from '@talend/react-components/lib';

function mapStateToProps(state, ownProps) {
	return {
		items: (ownProps.state && ownProps.state.get('items')) || [],
		maxItems: (ownProps.state && ownProps.state.get('maxItems')) || 10,
	};
}

export function ContainerBreadcrumbs(props) {
	const newProps = Object.assign({}, props);
	newProps.items = props.items.map(item => ({ ...item, onClick: (event, item) => props.dispatchActionCreator('folder:open', event, item) }));
	return <Breadcrumbs {...newProps} />;
}

ContainerBreadcrumbs.displayName = 'Breadcrumbs';

export default cmfConnect({
	defaultState: new Map({ items: [], maxItems: 10 }),
	mapStateToProps,
})(ContainerBreadcrumbs);
