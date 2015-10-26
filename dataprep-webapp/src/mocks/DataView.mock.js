/* jshint ignore:start */
function DataViewMock() {
	var filter, filterArgs, idProperty;
	var data = [];

	this.setItems = function(items, idPropertyObj){
		data = items;
		idProperty = idPropertyObj;
	};

	this.beginUpdate = function () {
	};
	this.endUpdate = function () {
	};

	this.setFilterArgs = function (args) {
		filterArgs = args;
	};

	this.setFilter = function (args) {
		filter = args;
	};

	//Must call setItems before to populate the data
	this.filter = function (data) {
		return filter(data, filterArgs);
	};

	//Must call setItems before to populate the data
	this.getIdxById = function (tdpId) {
		var record = _.find(data, {tdpId: tdpId});
		return record ? data.indexOf(record) : null;
	};

	//Must call setItems before to populate the data
	this.getItemById = function (tdpId) {
		return _.find(data, {tdpId: tdpId});
	};

	//Must call setItems before to populate the data
	this.getItem = function(rowIndex){
		return data[rowIndex];
	};

	//Must call setItems before to populate the data
	this.getItems = function(){
		return data;
	};

	//Must call setItems before to populate the data
	this.getLength = function(){
		return data.length;
	};

	this.insertItem = function () {
	};
	this.deleteItem = function () {
	};
	this.updateItem = function () {
	};

	this.onRowCountChanged = {
		subscribe: function () {}
	};
	this.onRowsChanged = {
		subscribe: function () {}
	};
}
/* jshint ignore:end */