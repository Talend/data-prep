/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('REST urls service', function() {
    'use strict';

    beforeEach(angular.mock.module('data-prep.services.utils'));
    beforeEach(inject(function (RestURLs) {
        RestURLs.setServerUrl('');
    }));
    
    it('should init api urls with empty server url (same url by default)', inject(function(RestURLs) {
        //then
        expect(RestURLs.datasetUrl).toBe('/api/datasets');
        expect(RestURLs.transformUrl).toBe('/api/transform');
        expect(RestURLs.preparationUrl).toBe('/api/preparations');
        expect(RestURLs.previewUrl).toBe('/api/preparations/preview');
        expect(RestURLs.exportUrl).toBe('/api/export');
        expect(RestURLs.aggregationUrl).toBe('/api/aggregate');
    }));

    it('should change api url with provided server url', inject(function(RestURLs) {
        //when
        RestURLs.setServerUrl('http://10.10.10.10:8888');

        //then
        expect(RestURLs.datasetUrl).toBe('http://10.10.10.10:8888/api/datasets');
        expect(RestURLs.transformUrl).toBe('http://10.10.10.10:8888/api/transform');
        expect(RestURLs.preparationUrl).toBe('http://10.10.10.10:8888/api/preparations');
        expect(RestURLs.previewUrl).toBe('http://10.10.10.10:8888/api/preparations/preview');
        expect(RestURLs.exportUrl).toBe('http://10.10.10.10:8888/api/export');
        expect(RestURLs.aggregationUrl).toBe('http://10.10.10.10:8888/api/aggregate');
    }));
});