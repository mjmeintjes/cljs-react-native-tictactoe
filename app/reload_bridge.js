/*
 * @providesModule ReloadBridge
 */

var CLOSURE_UNCOMPILED_DEFINES = null;

var config = {
    basePath: 'build/main.out/',
    googBasePath: 'goog/'
};


// Uninstall watchman???
function importJs(src, success, error){
    if(typeof success !== 'function') { success = function(){}; }
    if(typeof error !== 'function') { error = function(){}; }

    console.log('(Figwheel Bridge) Importing: ' + config.basePath + src);
    try {
        src = src.replace("main.out/", "");
        importScripts(config.basePath + src);
        success();
    } catch(e) {
        console.warn('Could not load: ' + config.basePath + src);
        console.error('Import error: ' + e);
        error();
    }
}

// Loads base goog js file then cljs_deps, goog.deps, core project cljs, and then figwheel
// Also calls the function to shim goog.require and goog.net.jsLoader.load
var shimmed = false;
function startEverything() {
    if(!shimmed) {
        shimmed = true;
        console.log('Loading Closure base.');
        importJs('goog/base.js');
        console.log(goog);
        shimBaseGoog();
        fakeLocalStorageAndDocument();
        importJs('cljs_deps.js');
        importJs('goog/deps.js');
        importJs('adzerk/boot_reload.js');

        shimJsLoader();
        adzerk.boot_reload.display.display = function (){};
        adzerk.boot_reload.reload.reload_html = function (){};
        adzerk.boot_reload.reload.reload_css = function (){};
        adzerk.boot_reload.reload.reload_img = function (){};


        console.log('Done loading Figwheel and Clojure app');
    }
}

function shimBaseGoog(){
    goog.basePath = 'goog/';
    goog.writeScriptSrcNode = importJs;
    goog.writeScriptTag_ = function(src, opt_sourceText){
        importJs(src);
        return true;
    };
    goog.inHtmlDocument_ = function(){ return true; };
}

function fakeLocalStorageAndDocument() {
    window.localStorage = {};
    window.localStorage.getItem = function(){ return 'true'; };
    window.localStorage.setItem = function(){};

    window.document = {};
    window.document.body = {};
    window.document.body.dispatchEvent = function(){};
    window.document.createElement = function(){};
}
module.exports = {
    start: startEverything,
    config: config
};
// Used by figwheel - uses importScript to load JS rather than <script>'s
function shimJsLoader(){
    goog.net.jsloader.load = function(uri, options) {
        var deferred = {
            callbacks: [],
            errbacks: [],
            addCallback: function(cb){
                deferred.callbacks.push(cb);
            },
            addCallbacks: function(cb, eb) {
                deferred.callbacks.push(cb);
                deferred.errbacks.push(cb);
            },
            addErrback: function(cb){
                deferred.errbacks.push(cb);
            },
            callAllCallbacks: function(){
                while(deferred.callbacks.length > 0){
                    deferred.callbacks.shift()();
                }
            },
            callAllErrbacks: function(){
                while(deferred.errbacks.length > 0){
                    deferred.errbacks.shift()();
                }
            }
        };

        if (typeof uri === 'string' || uri instanceof String) {
            //already a string
        }
        else {
            uri = uri.getPath();
        }
        importJs(uri, deferred.callAllCallbacks, deferred.callAllErrbacks);


        return deferred;
    };
}
