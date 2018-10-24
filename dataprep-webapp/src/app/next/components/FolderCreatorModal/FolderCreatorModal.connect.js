import { cmfConnect } from '@talend/react-cmf';
import { Map } from 'immutable';
import FolderCreatorModal from './FolderCreatorModal.component';


export default cmfConnect({
	componentId: 'default',
	defaultState: new Map({ show: false }),
})(FolderCreatorModal);
