import { call } from 'redux-saga/effects';
import api from '@talend/react-cmf';
import * as effects from '../../effects/preparation.effects';
import { IMMUTABLE_STATE } from './preparation.effects.mock';
import http from '../http';
import PreparationService from '../../../services/preparation.service';


describe('preparation', () => {
	describe('cancelRename', () => {
		it('should update preparations in the cmf store', () => {
			const preparation = 'id0';
			const gen = effects.cancelRename(preparation);
			expect(gen.next().value.SELECT).toBeDefined();
			const effect = gen.next(IMMUTABLE_STATE).value.PUT.action;
			expect(effect.type).toBe('REACT_CMF.COLLECTION_ADD_OR_REPLACE');
			expect(effect.collectionId).toBe('preparations');
			const prepUpdated = effect.data.find(prep => prep.get('id') === preparation);
			expect(prepUpdated.get('display')).toEqual('text');

			expect(gen.next().done).toBeTruthy();
		});
	});

	describe('setTitleEditionMode', () => {
		it('should update preparations in the cmf store', () => {
			const preparation = 'id0';
			const gen = effects.setTitleEditionMode(preparation);
			expect(gen.next().value.SELECT).toBeDefined();
			const effect = gen.next(IMMUTABLE_STATE).value.PUT.action;
			expect(effect.type).toBe('REACT_CMF.COLLECTION_ADD_OR_REPLACE');
			expect(effect.collectionId).toBe('preparations');
			const prepUpdated = effect.data.find(prep => prep.get('id') === preparation);
			expect(prepUpdated.get('display')).toEqual('input');

			expect(gen.next().done).toBeTruthy();
		});
	});

	describe('openPreparationCreator', () => {
		it('should update PreparationCreatorModal state in the cmf store', () => {
			const gen = effects.openPreparationCreator();
			const effect = gen.next().value.PUT.action;

			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.key).toEqual('default');
			expect(effect.componentName).toEqual('PreparationCreatorModal');
			expect(effect.componentState).toEqual({ show: true });

			expect(gen.next().done).toBeTruthy();
		});
	});

	describe('duplicate', () => {
		it('should duplicate the preparation and fetch the new preparations list', () => {
			const gen = effects.duplicate({ payload: { id: 'id0' } });
			const effect = gen.next().value.CALL;
			expect(effect.fn).toEqual(http.post);
			expect(effect.args[0].includes('/api/preparations/id0/copy?destination=Lw==&newName=test')).toBeTruthy();
			expect(gen.next().value).toEqual(call(effects.fetch));

			expect(gen.next().done).toBeTruthy();
		});
	});

	describe('fetch', () => {
		it('should fetch the preparations of the default folder', () => {
			const gen = effects.fetch({});
			const effect = gen.next().value.PUT.action;
			expect(effect.type).toEqual('GET');
			expect(effect.url).toEqual('/api/folders/Lw==/preparations');
			expect(effect.cmf).toEqual({ collectionId: 'preparations' });
			expect(effect.transform).toEqual(PreparationService.transform);

			expect(gen.next().done).toBeTruthy();
		});

		it('should fetch the preparations of a folder', () => {
			const gen = effects.fetch({ folderId: 'abcd' });
			const effect = gen.next().value.PUT.action;
			expect(effect.type).toEqual('GET');
			expect(effect.url).toEqual('/api/folders/abcd/preparations');
			expect(effect.cmf).toEqual({ collectionId: 'preparations' });
			expect(effect.transform).toEqual(PreparationService.transform);

			expect(gen.next().done).toBeTruthy();
		});
	});

	describe('rename', () => {
		it('should rename the preparation and fetch the new preparations list', () => {
			const gen = effects.rename({ id: 'id0', name: 'newPrep0' });
			const effect = gen.next().value.CALL;
			expect(effect.fn).toEqual(http.put);
			expect(effect.args[0]).toEqual('/api/preparations/id0');
			expect(effect.args[1]).toEqual({ name: 'newPrep0' });
			expect(gen.next().value).toEqual(call(effects.fetch));
			expect(gen.next().done).toBeTruthy();
		});
	});

	describe('openFolder', () => {
		it('should dispatch the appropriate action', () => {
			api.saga.putActionCreator = jest.fn();
			const gen = effects.openFolder({ id: 'test' });

			gen.next();

			expect(api.saga.putActionCreator).toHaveBeenCalledWith(
				'preparation:fetch',
				{
					folderId: {
						id: 'test',
					},
				},
			);

			expect(gen.next().done).toBeTruthy();
		});
	});
});
