import { cmfConnect } from '@talend/react-cmf';
import { Map } from 'immutable';
import { Breadcrumbs } from '@talend/react-components/lib';

function mapStateToProps(state, ownProps) {
	return {
		items: (ownProps.state && ownProps.state.get('items')) || [],
		maxItems: (ownProps.state && ownProps.state.get('maxItems')) || 10,
	};
}

Breadcrumbs.displayName = 'Breadcrumbs';
export default cmfConnect({
	defaultState: new Map({ items: [{ text: 'Text B', title: 'Text title B' }] }),
	mapStateToProps,
})(Breadcrumbs);
