import { call, take } from 'redux-saga/effects';
import sagas from '../folder.saga';
import * as effects from '../../effects/folder.effects';
import * as actions from '../../../constants/actions';


describe('folder', () => {
	describe('closeRemoveFolderModal', () => {
		it('should wait for CLOSE_REMOVE_FOLDER_MODAL action and call closeRemoveFolderModal', () => {
			const gen = sagas['folder:closeRemoveFolderConfirmDialog']();

			expect(gen.next().value).toEqual(take(actions.CLOSE_REMOVE_FOLDER_MODAL));
			expect(gen.next().value).toEqual(call(effects.closeRemoveFolderModal));

			expect(gen.next().value).toEqual(take(actions.CLOSE_REMOVE_FOLDER_MODAL));
		});
	});

	describe('openRemoveFolderModal', () => {
		it('should wait for OPEN_REMOVE_FOLDER_MODAL action and call openRemoveFolderModal', () => {
			const gen = sagas['folder:openRemoveFolderConfirmDialog']();
			const action = {
				payload: { id: 'folderId' },
			};
			expect(gen.next().value).toEqual(take(actions.OPEN_REMOVE_FOLDER_MODAL));
			expect(gen.next(action).value).toEqual(call(effects.openRemoveFolderModal, action.payload));

			expect(gen.next().value).toEqual(take(actions.OPEN_REMOVE_FOLDER_MODAL));
		});
	});

	describe('addFolder', () => {
		it('should wait for ADD_FOLDER action and call addFolder', () => {
			const gen = sagas['folder:add']();

			expect(gen.next().value).toEqual(take(actions.ADD_FOLDER));
			expect(gen.next().value).toEqual(call(effects.addFolder));

			expect(gen.next().value).toEqual(take(actions.ADD_FOLDER));
		});
	});
});
