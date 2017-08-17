/*  ============================================================================

  Copyright (C) 2006-2017 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Analytics account service', () => {
    'use strict';

    beforeEach(angular.mock.module('data-prep.services.utils'));

    it('should set value by default', inject((analyticsAccount) => {
        //then
        expect(analyticsAccount).toBe('');
    }));
});
