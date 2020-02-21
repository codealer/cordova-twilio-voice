
function TwilioVoice() {}

TwilioVoice.prototype.callbacks = {};
TwilioVoice.prototype.on = function(name, callback) {
    this.callbacks[name] = callback;
}

TwilioVoice.prototype.emit = function(name, params) {
    if (this.callbacks.hasOwnProperty(name)) {
        this.callbacks[name](params);
    }
}

TwilioVoice.prototype.initialize = function(token) {
    const onSuccess = (params) => {
        if (params && params.event) {
            var eventData = params.data || {};
            if (eventData.indexOf('[') !== -1 || eventData.indexOf('{') !== -1) {
                eventDate = JSON.parse(eventData);
            }
            this.emit(params.event, eventData);
        }
    }

    const onError = (error) => {
        this.emit('error', error);
    }

    cordova.exec(onSuccess, onError, 'TwilioVoice', 'setToken', [token]);
}

TwilioVoice.prototype.connect = function(params) {
    cordova.exec(null, null, 'TwilioVoice', 'connect', [params]);
}

TwilioVoice.prototype.disconnect = function() {
    cordova.exec(null, null, 'TwilioVoice', 'disconnect', []);
}

TwilioVoice.prototype.toggleMute = function() {
    cordova.exec(null, null, 'TwilioVoice', 'toggleMute', []);
}

TwilioVoice.prototype.toggleSpeaker = function() {
    cordova.exec(null, null, 'TwilioVoice', 'toggleSpeaker', []);
}

module.exports = new TwilioVoice();