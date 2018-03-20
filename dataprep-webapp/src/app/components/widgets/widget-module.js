/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import ngTranslate from 'angular-translate';
import ngAnimate from 'angular-animate';

import BOOTSTRAP_COLLAPSE from 'angular-ui-bootstrap/src/collapse';
import BOOTSTRAP_DROPDOWN from 'angular-ui-bootstrap/src/dropdown/index-nocss';
import BOOTSTRAP_MODAL from 'angular-ui-bootstrap/src/modal/index-nocss';

import TalendBadge from './badge/widget-badge-component';
import ResizableInput from './resizable-input/resizable-input-directive';
import TalendButtonDropdown from './button-dropdown/widget-button-dropdown-directive';
import TalendButtonSwitch from './button-switch/widget-button-switch-directive';
import BoxplotChart from './charts/boxplot-chart/boxplot-chart-directive';
import HorizontalBarchart from './charts/horizontal-barchart/horizontal-barchart-directive';
import RangeSliderCtrl from './charts/range-slider/range-slider-controller';
import RangeSlider from './charts/range-slider/range-slider-directive';
import VerticalBarchart from './charts/vertical-barchart/vertical-barchart-directive';
import TalendConfirmService from './confirm/widget-confirm-service';
import TalendConfirmCtrl from './confirm/widget-confirm-controller';
import TalendConfirm from './confirm/widget-confirm-directive';
import TalendDatetimePicker from './datetimepicker/widget-datetimepicker-directive';
import TalendDropdown from './dropdown/widget-dropdown-directive';
import TalendEditableRegexCtrl from './editable-regex/widget-editable-regex-controller';
import TalendEditableRegex from './editable-regex/widget-editable-regex-directive';
import EditableSelect from './editable-select/widget-editable-select-directive';
import TalendEditableText from './editable-text/widget-editable-text-directive';
import TalendFileSelector from './file-selector/widget-file-selector-directive';
import TalendLoading from './loading/widget-loading-directive';
import TalendModal from './modal/widget-modal-directive';
import NavigationList from './navigation-list/widget-navigation-list-directive';
import QualityBarCtrl from './quality-bar/widget-quality-bar-controller';
import QualityBar from './quality-bar/widget-quality-bar-directive';
import TalendSearchInput from './search/widget-search-input-directive';
import TalendSlidable from './slidable/widget-slidable-directive';
import TalendTooltipCtrl from './tooltip/widget-tooltip-controller';
import TalendTooltip from './tooltip/widget-tooltip-directive';
import ListItemHandler from './list-item-handler/list-item-handler-component';
import AutoScroll from './auto-scroll/auto-scroll-directive';

const MODULE_NAME = 'talend.widget';

/**
 * @ngdoc object
 * @name talend.widget
 * @description This module contains all the reusable widgets
 */
angular.module(MODULE_NAME, [
	ngTranslate,
	ngAnimate,
	BOOTSTRAP_COLLAPSE,
	BOOTSTRAP_DROPDOWN,
	BOOTSTRAP_MODAL,
])
    .component('talendBadge', TalendBadge)
    .directive('resizableInput', ResizableInput)

    .directive('talendButtonDropdown', TalendButtonDropdown)

    .directive('talendButtonSwitch', TalendButtonSwitch)

    .directive('boxplotChart', BoxplotChart)
    .directive('horizontalBarchart', HorizontalBarchart)
    .controller('RangeSliderCtrl', RangeSliderCtrl)
    .directive('rangeSlider', RangeSlider)
    .directive('verticalBarchart', VerticalBarchart)

    .service('TalendConfirmService', TalendConfirmService)
    .controller('TalendConfirmCtrl', TalendConfirmCtrl)
    .directive('talendConfirm', TalendConfirm)

    .directive('talendDatetimePicker', TalendDatetimePicker)

    .directive('talendDropdown', TalendDropdown)

    .controller('TalendEditableRegexCtrl', TalendEditableRegexCtrl)
    .directive('talendEditableRegex', TalendEditableRegex)

    .directive('editableSelect', EditableSelect)

    .directive('talendEditableText', TalendEditableText)

    .directive('talendFileSelector', TalendFileSelector)

    .directive('talendLoading', TalendLoading)

    .directive('talendModal', TalendModal)

    .directive('navigationList', NavigationList)

    .controller('QualityBarCtrl', QualityBarCtrl)
    .directive('qualityBar', QualityBar)

    .directive('talendSearchInput', TalendSearchInput)

    .directive('talendSlidable', TalendSlidable)

    .controller('TalendTooltipCtrl', TalendTooltipCtrl)
    .directive('talendTooltip', TalendTooltip)
    .component('listItemHandler', ListItemHandler)
    .directive('autoScroll', AutoScroll);

export default MODULE_NAME;
