var exec = require('cordova/exec');

function WacomSlate() {}

WacomSlate.prototype.onEvent = function( callback, success, error ){
    WacomSlate.prototype.onEventReceived = callback;
    exec(success, error, "WacomSlate", "start", []);
};

WacomSlate.prototype.onEventReceived = function(payload){
    console.log("Pen event received");
};

WacomSlate.prototype.onData = function( callback, success, error ){
    WacomSlate.prototype.onDataReceived = callback;
    exec(success, error, "WacomSlate", "start", []);
};

WacomSlate.prototype.onDataReceived = function(payload){
    console.log("Pen data received");
};

var wacomSlate = new WacomSlate();
module.exports = wacomSlate;

WacomSlate.install = function () {
    if (!window.plugins) {
        window.plugins = {};
    }
    window.plugins.WacomSlate = new WacomSlate();
    return window.plugins.WacomSlate;
};
cordova.addConstructor(WacomSlate.install);