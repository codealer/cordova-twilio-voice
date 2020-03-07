
function TwilioVoice() {}

TwilioVoice.prototype.callbacks = {};
TwilioVoice.prototype.serviceId = cordova.platformId == 'ios' ? 'TwilioVoiceOutgoing' : 'TwilioVoice';
TwilioVoice.prototype.on = function(name, callback) {
    this.callbacks[name] = callback;
}

TwilioVoice.prototype.emit = function(name, params) {
console.log('emit', name, params);
    if (this.callbacks.hasOwnProperty(name)) {
        this.callbacks[name](params);
    }
}

TwilioVoice.prototype.initialize = function(token) {
    cordova.exec(TwilioVoiceSuccessCallback, TwilioVoiceErrorCallback, this.serviceId, 'setToken', [token]);
}

TwilioVoice.prototype.connect = function(params) {
    cordova.exec(TwilioVoiceSuccessCallback, TwilioVoiceErrorCallback, this.serviceId, 'connect', [params]);
}

TwilioVoice.prototype.disconnect = function() {
    cordova.exec(TwilioVoiceSuccessCallback, TwilioVoiceErrorCallback, this.serviceId, 'disconnect', []);
}

TwilioVoice.prototype.toggleMute = function() {
    cordova.exec(TwilioVoiceSuccessCallback, TwilioVoiceErrorCallback, this.serviceId, 'toggleMute', []);
}

TwilioVoice.prototype.toggleSpeaker = function() {
    cordova.exec(TwilioVoiceSuccessCallback, TwilioVoiceErrorCallback, this.serviceId, 'toggleSpeaker', []);
}

function TwilioVoiceSuccessCallback(params) {
    if (params && params.event) {
        var eventData = params.data || {};
        if (eventData.indexOf('[') !== -1 || eventData.indexOf('{') !== -1) {
            eventDate = JSON.parse(eventData);
        }
        TwilioVoiceInstance.emit(params.event, eventData);
    }
}

function TwilioVoiceErrorCallback(error) {
    TwilioVoiceInstance.emit('error', error);
}

window.TwilioVoiceInstance = new TwilioVoice();

module.exports = TwilioVoiceInstance;