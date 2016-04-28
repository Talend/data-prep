/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Folder controller', () => {
    'use strict';

    let createController, scope;
    const children = [
        { path: '/my/toto/tutu', name: 'tutu' },
        { path: '/my/toto/titi', name: 'titi' },
    ];

    beforeEach(angular.mock.module('data-prep.folder'));

    beforeEach(inject(($rootScope, $controller, $q, FolderService, StateService) => {
        scope = $rootScope.$new();

        createController = () => $controller('FolderCtrl', { $scope: scope });

        spyOn(FolderService, 'children').and.returnValue($q.when(children));
        spyOn(StateService, 'setMenuChildren').and.returnValue();
    }));

    it('should fetch folder children', inject((FolderService) => {
        // given
        const ctrl = createController();

        // when
        ctrl.initMenuChildren({ path: '/my/toto', name: 'toto' });

        // then
        expect(FolderService.children).toHaveBeenCalledWith('/my/toto');
    }));

    it('should set menu children state', inject((StateService) => {
        // given
        const ctrl = createController();
        expect(StateService.setMenuChildren).not.toHaveBeenCalled();

        // when
        ctrl.initMenuChildren({ path: '/my/toto', name: 'toto' });
        expect(StateService.setMenuChildren).toHaveBeenCalledWith([]);
        scope.$digest();

        // then
        expect(StateService.setMenuChildren).toHaveBeenCalledWith(children);
    }));
});
