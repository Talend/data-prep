/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Copyrights service', () => {
    'use strict';

    beforeEach(angular.mock.module('data-prep.services.utils'));

    it('should set value by default', inject((copyRights) => {
        //then
        expect(copyRights).toBe('Talend. All Rights Reserved');
    }));
});
