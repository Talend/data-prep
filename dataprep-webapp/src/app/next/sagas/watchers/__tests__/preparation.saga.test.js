import { all, call, take } from 'redux-saga/effects';
import sagas from '../preparation.saga';
import * as effects from '../../effects/preparation.effects';
import * as actions from '../../../constants/actions';

describe('preparation', () => {
	describe('cancelRename', () => {
		it('should wait for CANCEL_RENAME_PREPARATION action and call cancelRename', () => {
			const gen = sagas['preparation:rename:cancel']();
			const action = {
				payload: { id: 'prepId' },
			};

			expect(gen.next().value).toEqual(take(actions.CANCEL_RENAME_PREPARATION));
			expect(gen.next(action).value).toEqual(call(effects.cancelRename, action.payload));

			expect(gen.next().value).toEqual(take(actions.CANCEL_RENAME_PREPARATION));
		});
	});

	describe('fetch', () => {
		it('should wait for FETCH_PREPARATIONS action and call fetch', () => {
			const gen = sagas['preparation:fetch']();
			const action = {
				payload: { folderId: 'folderId' },
			};

			expect(gen.next().value).toEqual(take(actions.FETCH_PREPARATIONS));
			expect(gen.next(action).value).toEqual(call(effects.refresh, action.payload));
			expect(gen.next().value).toEqual(take(actions.FETCH_PREPARATIONS));
		});
	});

	describe('rename', () => {
		it('should wait for RENAME_PREPARATION action and call rename', () => {
			const gen = sagas['preparation:rename:submit']();
			const action = {
				payload: { id: 'prepId' },
			};

			expect(gen.next().value).toEqual(take(actions.RENAME_PREPARATION));
			expect(gen.next(action).value).toEqual(call(effects.rename, action.payload));

			expect(gen.next().value).toEqual(take(actions.RENAME_PREPARATION));
		});
	});
	describe('copy', () => {
		it('should wait for PREPARATION_COPY action and call copy', () => {
			const gen = sagas['preparation:copy']();
			const action = {
				payload: { id: 'prepId' },
			};

			expect(gen.next().value).toEqual(take(actions.PREPARATION_COPY));
			expect(gen.next(action).value).toEqual(call(effects.copy, action.payload));

			expect(gen.next().value).toEqual(take(actions.PREPARATION_COPY));
		});
	});
	describe('move', () => {
		it('should wait for PREPARATION_MOVE action and call move', () => {
			const gen = sagas['preparation:move']();
			const action = {
				payload: { id: 'prepId' },
			};

			expect(gen.next().value).toEqual(take(actions.PREPARATION_MOVE));
			expect(gen.next(action).value).toEqual(call(effects.move, action.payload));

			expect(gen.next().value).toEqual(take(actions.PREPARATION_MOVE));
		});
	});
	describe('setTitleEditionMode', () => {
		it('should wait for SET_TITLE_EDITION_MODE action and call setTitleEditionMode', () => {
			const gen = sagas['preparation:rename']();
			const action = {
				payload: { id: 'prepId' },
			};

			expect(gen.next().value).toEqual(take(actions.SET_TITLE_EDITION_MODE));
			expect(gen.next(action).value).toEqual(call(effects.setTitleEditionMode, action.payload));

			expect(gen.next().value).toEqual(take(actions.SET_TITLE_EDITION_MODE));
		});
	});

	describe('openCopyModal', () => {
		it('should wait for OPEN_COPY_MODAL action and call fetchTree and openCopyModal', () => {
			const gen = sagas['preparation:copy:open']();
			const action = {
				payload: { id: 'prepId' },
			};
			expect(gen.next().value).toEqual(take(actions.OPEN_COPY_MODAL));
			expect(gen.next(action).value).toEqual(all([call(effects.fetchTree), call(effects.openCopyMoveModal, action.payload, 'copy')]));

			expect(gen.next().value).toEqual(take(actions.OPEN_COPY_MODAL));
		});
	});

	describe('openMoveModal', () => {
		it('should wait for OPEN_MOVE_MODAL action and call fetchTree and openMoveModal', () => {
			const gen = sagas['preparation:move:open']();
			const action = {
				payload: { id: 'prepId' },
			};
			expect(gen.next().value).toEqual(take(actions.OPEN_MOVE_MODAL));
			expect(gen.next(action).value).toEqual(all([call(effects.fetchTree), call(effects.openCopyMoveModal, action.payload, 'move')]));

			expect(gen.next().value).toEqual(take(actions.OPEN_MOVE_MODAL));
		});
	});

	describe('closeCopyMoveModal', () => {
		it('should wait for OPEN_PREPARATION_CREATOR action and call openPreparationCreator', () => {
			const gen = sagas['preparation:copy:move:cancel']();

			expect(gen.next().value).toEqual(take(actions.CLOSE_COPY_MOVE_MODAL));
			expect(gen.next().value).toEqual(call(effects.closeCopyMoveModal));

			expect(gen.next().value).toEqual(take(actions.CLOSE_COPY_MOVE_MODAL));
		});
	});
});
