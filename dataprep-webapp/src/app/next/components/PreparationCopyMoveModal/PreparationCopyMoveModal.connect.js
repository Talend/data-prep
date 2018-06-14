import { cmfConnect } from '@talend/react-cmf';
import { Map } from 'immutable';
import PreparationCropyMoveModal from './PreparationCopyMoveModal.component';

export default cmfConnect({
	componentId: 'default',
	defaultState: new Map({ show: false }),
})(PreparationCropyMoveModal);
