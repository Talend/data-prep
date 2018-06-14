import { call, take } from 'redux-saga/effects';
import sagas from '../preparation.saga';
import * as effects from '../../effects/preparation.effects';
import {
	CANCEL_RENAME_PREPARATION,
	FETCH_PREPARATIONS,
	OPEN_FOLDER,
	OPEN_PREPARATION_CREATOR,
	PREPARATION_DUPLICATE,
	RENAME_PREPARATION,
	SET_TITLE_EDITION_MODE,
} from '../../../constants/actions';

describe('preparation', () => {
	describe('cancelRename', () => {
		it('should wait for CANCEL_RENAME_PREPARATION action and call cancelRename', () => {
			const gen = sagas['preparation:rename:cancel']();
			const action = {
				payload: { id: 'prepId' },
			};

			expect(gen.next().value).toEqual(take(CANCEL_RENAME_PREPARATION));
			expect(gen.next(action).value).toEqual(call(effects.cancelRename, action.payload));

			expect(gen.next().value).toEqual(take(CANCEL_RENAME_PREPARATION));
		});
	});

	describe('duplicate', () => {
		it('should wait for PREPARATION_DUPLICATE action and call duplicate', () => {
			const gen = sagas['preparation:duplicate']();
			const prep = [{ id: 'prepId' }];

			expect(gen.next().value).toEqual(take(PREPARATION_DUPLICATE));
			expect(gen.next(prep).value).toEqual(call(effects.duplicate, prep));

			expect(gen.next().value).toEqual(take(PREPARATION_DUPLICATE));
		});
	});

	describe('fetch', () => {
		it('should wait for FETCH_PREPARATIONS action and call fetch', () => {
			const gen = sagas['preparation:fetch']();
			const action = {
				payload: { folderId: 'folderId' },
			};

			expect(gen.next().value).toEqual(take(FETCH_PREPARATIONS));
			expect(gen.next(action).value).toEqual(call(effects.fetch, action.payload));

			expect(gen.next().value).toEqual(take(FETCH_PREPARATIONS));
		});
	});

	describe('openFolder', () => {
		it('should wait for OPEN_FOLDER action and call openFolder', () => {
			const gen = sagas['preparation:folder:open']();
			const action = {
				id: 'folderId',
			};

			expect(gen.next().value).toEqual(take(OPEN_FOLDER));
			expect(gen.next(action).value).toEqual(call(effects.openFolder, action.id));

			expect(gen.next().value).toEqual(take(OPEN_FOLDER));
		});
	});

	describe('rename', () => {
		it('should wait for RENAME_PREPARATION action and call rename', () => {
			const gen = sagas['preparation:rename:submit']();
			const action = {
				payload: { id: 'prepId' },
			};

			expect(gen.next().value).toEqual(take(RENAME_PREPARATION));
			expect(gen.next(action).value).toEqual(call(effects.rename, action.payload));

			expect(gen.next().value).toEqual(take(RENAME_PREPARATION));
		});
	});

	describe('setTitleEditionMode', () => {
		it('should wait for SET_TITLE_EDITION_MODE action and call setTitleEditionMode', () => {
			const gen = sagas['preparation:rename']();
			const action = {
				payload: { id: 'prepId' },
			};

			expect(gen.next().value).toEqual(take(SET_TITLE_EDITION_MODE));
			expect(gen.next(action).value).toEqual(call(effects.setTitleEditionMode, action.payload));

			expect(gen.next().value).toEqual(take(SET_TITLE_EDITION_MODE));
		});
	});

	describe('openPreparationCreator', () => {
		it('should wait for OPEN_PREPARATION_CREATOR action and call openPreparationCreator', () => {
			const gen = sagas['preparation:creator:open']();

			expect(gen.next().value).toEqual(take(OPEN_PREPARATION_CREATOR));
			expect(gen.next().value).toEqual(call(effects.openPreparationCreator));

			expect(gen.next().value).toEqual(take(OPEN_PREPARATION_CREATOR));
		});
	});
});
