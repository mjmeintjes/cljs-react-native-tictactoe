/*
 * @providesModule ReloadBridge
 */

var CLOSURE_UNCOMPILED_DEFINES = null;

var config = {
    basePath: '/build',
};


// Uninstall watchman???
function importJs(src, success, error){
    if(typeof success !== 'function') { success = function(){}; }
    if(typeof error !== 'function') { error = function(){}; }
    console.log('importing ' + src);

    //Only reload React Native modules created by boot task
    // fetch("http://matt-dev:8081/" + config.basePath + src)
        // .then(function(response) {
            // return response.text();
        // })
        // .then(function(script) {
            // eval(script);
        // })
        // .then(success)
        // .catch(error);
    // return;
    if (!src.startsWith('/node_modules')) {
        success();
        return;
    }

    console.log('(Reload Bridge) Importing: ' + src);
    
    try {
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
        fakeLocalStorageAndDocument();
        shimBaseGoog();
        console.log('Starting shim setup');

        shimJsLoader();
        shimAdzerkReload();

        console.log('Done shimming');
    }
}
function setupShims() {
    shimBaseGoog();
    fakeLocalStorageAndDocument();
    shimJsLoader();
    shimAdzerkReload();
}

function shimAdzerkReload() {
    // var old = adzerk.boot_reload.reload.reload_js;
    // adzerk.boot_reload.reload.reload_js = function(a, b) {
    //     fetch('http://matt-dev:8000/FETCHING');
    //     old(a,b);
    // };

    adzerk.boot_reload.display.display = function (){};
    adzerk.boot_reload.reload.reload_html = function (){};
    adzerk.boot_reload.reload.reload_css = function (){};
    adzerk.boot_reload.reload.reload_img = function (){};
}
function shimBaseGoog(){
    //goog.basePath = 'goog/';
    global.CLOSURE_BASE_PATH = 'goog/';
    global.CLOSURE_IMPORT_SCRIPT = function(src, opt_sourceText){
        importJs(src);
        return true;
    };
    //goog.writeScriptSrcNode = importJs;
    //goog.inHtmlDocument_ = function(){ return true; };
}

function fakeLocalStorageAndDocument() {
    window.localStorage = {};
    window.localStorage.getItem = function(){ return 'true'; };
    window.localStorage.setItem = function(){};

    window.location = {};
    window.location.href = 'localhost';
    window.document = {};
    window.document.body = {};
    window.document.body.dispatchEvent = function(){};
    window.document.createElement = function(){};
}
module.exports = {
    start: startEverything,
    setupShims: setupShims,
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
        setTimeout(function(){
            importJs(uri, deferred.callAllCallbacks, deferred.callAllErrbacks); 
        }, 1)


        return deferred;
    };
}
