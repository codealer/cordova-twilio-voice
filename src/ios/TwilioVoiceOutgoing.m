#import "TwilioVoiceOutgoing.h"

@import AVFoundation;
@import TwilioVoice;

@interface TwilioVoiceOutgoing () <TVOCallDelegate>

@property (nonatomic, strong) TVOCall *activeCall;
@property (nonatomic, strong) TVODefaultAudioDevice *audioDevice;
@property (nonatomic, strong) NSString *accessToken;
@property (nonatomic, strong) NSString *jsCallbackId;
@property (nonatomic, assign) BOOL *speakerOn;

@end

@implementation TwilioVoiceOutgoing

- (void) pluginInitialize {
    [super pluginInitialize];
    
    self.audioDevice = [TVODefaultAudioDevice audioDevice];
    TwilioVoice.audioDevice = self.audioDevice;
    
    self.speakerOn = FALSE;
}

- (void) setToken:(CDVInvokedUrlCommand*)command {
    self.jsCallbackId = command.callbackId;
    NSString *currentToken = nil;
    currentToken = self.accessToken;
    self.accessToken = command.arguments[0];
    if (self.accessToken) {
        [self returnSuccess:@"tokenSet" :self.accessToken];
        [self returnSuccess:@"initialized" :self.accessToken];
    }
    else {
        [self returnError:@"token_missing" :@"Token parameter is missing" :@""];
    }
}

- (void) connect:(CDVInvokedUrlCommand*)command {
    self.jsCallbackId = command.callbackId;
    NSDictionary *params = command.arguments[0];

    [self.commandDelegate runInBackground:^{
        if (!self.activeCall || !(self.activeCall.state == TVOCallStateConnected && self.activeCall.state == TVOCallStateConnecting && self.activeCall.state == TVOCallStateRinging && self.activeCall.state == TVOCallStateReconnecting)) {
            self.speakerOn = FALSE;
            NSString *recipientParamName = @"To";
            TVOConnectOptions *connectOptions = [TVOConnectOptions optionsWithAccessToken:self.accessToken block:^(TVOConnectOptionsBuilder *builder) {
                builder.params = @{recipientParamName:params[@"To"]};
            }];
            self.activeCall = [TwilioVoice connectWithOptions:connectOptions delegate:self];
        }
    }];
}

- (void) disconnect:(CDVInvokedUrlCommand*)command {
    self.jsCallbackId = command.callbackId;
    if (self.activeCall) {
        [self.activeCall disconnect];
    }
}

- (void) toggleSpeaker:(CDVInvokedUrlCommand*)command {
    self.jsCallbackId = command.callbackId;
    if (self.activeCall) {
        BOOL newState = !self.speakerOn;
        self.speakerOn = &(newState);
        [self toggleAudioRoute:newState];
        NSString *newStateString = newState ? @"true" : @"false";
        [self returnSuccess:@"speakerToggled" :newStateString];
    }
}

- (void) toggleMute:(CDVInvokedUrlCommand*)command {
    self.jsCallbackId = command.callbackId;
    if (self.activeCall) {
        self.activeCall.muted = !self.activeCall.muted;
        NSString *mutedString = self.activeCall.muted ? @"true" : @"false";
        [self returnSuccess:@"muteToggled" :mutedString];
    }
}

- (void) returnResponse:(NSString *)type jsResponse:(NSDictionary *)response {
    CDVPluginResult *result;
    if ([type isEqualToString:@"success"]) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:response];
    }
    else {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:response];
    }
    [result setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:result callbackId:self.jsCallbackId];
}

- (void) returnSuccess:(NSString *)event :(NSString *)paramsJson {
    NSDictionary *params = [NSDictionary dictionaryWithObjectsAndKeys:event, @"event", paramsJson, @"data", nil];
    [self returnResponse :@"success" jsResponse:params];
}

- (void) returnError:(NSString *)errorId :(NSString *)errorMessage :(NSString *)paramsJson {
    NSDictionary *params = [NSDictionary dictionaryWithObjectsAndKeys:errorId, @"id", errorMessage, @"error", paramsJson, @"params", nil];
    [self returnResponse :@"error" jsResponse:params];
}

#pragma mark - TVOCallDelegate

- (void)callDidStartRinging:(TVOCall *)call {
    [self returnSuccess:@"statusChanged" :@"ringing"];
}

- (void)callDidConnect:(TVOCall *)call {
    [self returnSuccess:@"statusChanged" :@"connected"];
}

- (void)call:(TVOCall *)call isReconnectingWithError:(NSError *)error {
    [self returnSuccess:@"statusChanged" :@"reconnecting"];
}

- (void)call:(TVOCall *)call didFailToConnectWithError:(NSError *)error {
    [self returnSuccess:@"statusChanged" :@"connectFailure"];
    self.activeCall = nil;
}

- (void)call:(TVOCall *)call didDisconnectWithError:(NSError *)error {
    [self returnSuccess:@"statusChanged" :@"disconnected"];
    self.activeCall = nil;
}

- (void)callDidReconnect:(TVOCall *)call {
    [self returnSuccess:@"statusChanged" :@"reconnected"];
}

#pragma mark - AVAudioSession
- (void)toggleAudioRoute:(BOOL)toSpeaker {
    self.audioDevice.block =  ^ {
        kTVODefaultAVAudioSessionConfigurationBlock();
        AVAudioSession *session = [AVAudioSession sharedInstance];
        NSError *error = nil;
        if (toSpeaker) {
            if (![session overrideOutputAudioPort:AVAudioSessionPortOverrideSpeaker error:&error]) {
                NSLog(@"Unable to reroute audio: %@", [error localizedDescription]);
            }
        }
        else {
            if (![session overrideOutputAudioPort:AVAudioSessionPortOverrideNone error:&error]) {
                NSLog(@"Unable to reroute audio: %@", [error localizedDescription]);
            }
        }
    };
    self.audioDevice.block();
}

@end