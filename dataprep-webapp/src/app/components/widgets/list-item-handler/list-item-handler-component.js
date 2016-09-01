/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const ListItemHandler = {
    bindings: {
        onBottomClick: '&',
        onMiddleClick: '&',
        onTopClick: '&',
        showBottomArrow: '<',
        showTopArrow: '<',
    },
    template: `
        <div class="list-item-handler">
          <div class="top-handler"
              ng-if="$ctrl.showTopArrow"
              ng-click="$ctrl.onTopClick()">
          </div>

          <div class="middle-handler"
              ng-click="$ctrl.onMiddleClick()">
          </div>

          <div class="down-handler"
              ng-if="$ctrl.showBottomArrow"
              ng-click="$ctrl.onBottomClick()">
          </div>
        </div>`,
};

export default ListItemHandler;
