/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc controller
 * @name talend.widget.controller:TypeaheadCtrl
 * @description Typeahead directive controller
 */
class TypeaheadCtrl {

    constructor($q) {
        'ngInject';
        this.$q = $q;
        this.searchString = '';
    }

    onChange() {
        this.showResults();
        this.searching = true;
        const searchInput = this.searchString;

        this.$q.when(this.search({ value: this.searchString }))
            .then(() => {
                if (searchInput === this.searchString) {
                    this.searching = false;
                }
            });
    }
    
    hideResults() {
        this.visible = false;
    }

    showResults() {
        this.visible = true;
    }
}
export default TypeaheadCtrl;
