import { cmfConnect } from '@talend/react-cmf';
import { Map } from 'immutable';
import AboutModal from './AboutModal.component';

export default cmfConnect({
	defaultState: new Map({ show: false }),
})(AboutModal);
