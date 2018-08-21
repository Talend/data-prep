import { cmfConnect } from '@talend/react-cmf';
import AboutModal, { DEFAULT_STATE } from './AboutModal.component';

export default cmfConnect({
	componentId: 'default',
	defaultState: DEFAULT_STATE,
})(AboutModal);
