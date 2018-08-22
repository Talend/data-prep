import { cmfConnect } from '@talend/react-cmf';
import { Map } from 'immutable';
import PreparationCreatorModal from './PreparationCreatorModal.component';

export default cmfConnect({
	componentId: 'default',
	defaultState: new Map({ show: false }),
})(PreparationCreatorModal);
