/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Converter service', function () {
    'use strict';

    beforeEach(angular.mock.module('data-prep.services.utils'));

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            "INTEGER": "entier",
            "DECIMAL": "décimal",
            "BOOLEAN": "booléen",
            "TEXT": "texte",
            "DATE": "dateTime",
            "UNKNOWN": "inconnu"
        });
        $translateProvider.preferredLanguage('en');
    }));

    it('should return number when input type is numeric, integer, double or float', inject(function (ConverterService) {
        checkToInputType(ConverterService, ['numeric', 'integer', 'double', 'float'], 'number');
    }));

    it('should return text when input type is string', inject(function (ConverterService) {
        checkToInputType(ConverterService, ['string'], 'text');
    }));

    it('should return checkbox when input type is boolean', inject(function (ConverterService) {
        checkToInputType(ConverterService, ['boolean'], 'checkbox');
    }));

    it('should return checkbox when input type is password', inject(function (ConverterService) {
        checkToInputType(ConverterService, ['password'], 'password');
    }));

    it('should return text by default', inject(function (ConverterService) {
        checkToInputType(ConverterService, ['toto', 'titi', 'tata'], 'text');
    }));

    /**
     * @ngdoc method
     * @name checkToInputType
     * @methodOf data-prep.services.utils.service:ConverterServiceSpec
     * @param {Object} service - the converter service
     * @param {string[]} types - the types to convert
     * @param {string} expectedType - the expected type
     * @description check the checkToInputType function behaviour
     */
    var checkToInputType = function (service, types, expectedType) {
        for (var i = 0; i < types.length; i++) {

            //when
            var type = service.toInputType(types[i]);

            // then
            expect(type).toBe(expectedType);
        }
    };

    it('should return integer label when column type is numeric, integer', inject(function (ConverterService) {
        checkSimplifiedTypes(ConverterService, ['numeric', 'integer'], 'integer');
    }));

    it('should return decimal label when column type is double or float', inject(function (ConverterService) {
        checkSimplifiedTypes(ConverterService, ['double', 'float'], 'decimal');
    }));

    it('should return text label when column type is string or char', inject(function (ConverterService) {
        checkSimplifiedTypes(ConverterService, ['string', 'char'], 'text');
    }));

    it('should return boolean label when column type is boolean', inject(function (ConverterService) {
        checkSimplifiedTypes(ConverterService, ['boolean'], 'boolean');
    }));

    it('should return date label when column type is date', inject(function (ConverterService) {
        checkSimplifiedTypes(ConverterService, ['date'], 'date');
    }));

    it('should return unknown label when column type is unknown', inject(function (ConverterService) {
        checkSimplifiedTypes(ConverterService, ['toto', 'titi', 'tata', ''], 'unknown');
    }));

    /**
     * @ngdoc method
     * @name checkSimplifiedTypes
     * @methodOf data-prep.services.utils.service:ConverterServiceSpec
     * @param {Object} service - the converter service
     * @param {string[]} types - the types to convert
     * @param {string} expectedType - the expected type
     * @description Convert the given types and check against the expected one
     */
    var checkSimplifiedTypes = function (service, types, expectedType) {
        for (var i = 0; i < types.length; i++) {

            //when
            var type = service.simplifyType(types[i]);

            // then
            expect(type).toBe(expectedType);
        }
    };


    it('should return integer when column type is numeric, integer', inject(function (ConverterService) {
        checkSimplifiedTypesLabels(ConverterService, ['numeric', 'integer'], 'entier');
    }));

    it('should return decimal when column type is double or float', inject(function (ConverterService) {
        checkSimplifiedTypesLabels(ConverterService, ['double', 'float'], 'décimal');
    }));

    it('should return text when column type is string or char', inject(function (ConverterService) {
        checkSimplifiedTypesLabels(ConverterService, ['string', 'char'], 'texte');
    }));

    it('should return boolean when column type is boolean', inject(function (ConverterService) {
        checkSimplifiedTypesLabels(ConverterService, ['boolean'], 'booléen');
    }));

    it('should return date when column type is date', inject(function (ConverterService) {
        checkSimplifiedTypesLabels(ConverterService, ['date'], 'dateTime');
    }));

    it('should return unknown when column type is unknown', inject(function (ConverterService) {
        checkSimplifiedTypesLabels(ConverterService, ['toto', 'titi', 'tata', ''], 'inconnu');
    }));


    var checkSimplifiedTypesLabels = function (service, types, expectedTypeLabel) {
        for (var i = 0; i < types.length; i++) {

            //when
            var type = service.simplifyTypeLabel(types[i]);

            // then
            expect(type).toBe(expectedTypeLabel);
        }
    };

    it('should check numbers validity', inject(function (ConverterService) {
        //when
        var amIaNumber = ConverterService.isNumber('dqsfds10010');
        var amIaNumber2 = ConverterService.isNumber(' 88');
        var amIaNumber3 = ConverterService.isNumber('');

        //then
        expect(amIaNumber).toBe(false);
        expect(amIaNumber2).toBe(true);
        expect(amIaNumber3).toBe(false);
    }));
});
