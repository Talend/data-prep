/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Datepattern Service', () => {
    'use strict';



    beforeEach(angular.mock.module('data-prep.services.datepattern'));

    beforeEach(inject(($q, DatepatternRestService) => {
        spyOn(DatepatternRestService, 'search').and.returnValue($q.when());
    }));

    it('should call rest not parameter', inject((DatepatternService, DatepatternRestService) => {
        //when
        DatepatternService.search('');

        //then
        expect(DatepatternRestService.search).toHaveBeenCalledWith('');
    }));

    it('should call rest one parameter', inject((DatepatternService, DatepatternRestService) => {
       //when
       DatepatternService.search('beer');

        //then
        expect(DatepatternRestService.search).toHaveBeenCalledWith('beer');
    }));

});