import { cmfConnect } from '@talend/react-cmf';
import FolderCreatorModal, { DEFAULT_STATE } from './FolderCreatorModal.component';


export default cmfConnect({
	componentId: 'default',
	defaultState: DEFAULT_STATE,
})(FolderCreatorModal);
