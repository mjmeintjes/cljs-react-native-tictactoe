/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 */
'use strict';
var React = require('react-native');
var bridge = require('ReloadBridge');

var {
  AppRegistry,
  View,
  Text
} = React;

var appRoot = React.createClass({
  render: function() {
    return (
      <View style={{flex:1,alignItems:'center',justifyContent:'center'}}>
        <Text>Loading application...</Text>
      </View>
    );
  }
});

console.log('starting bridge');

AppRegistry.registerComponent('tictactoe', () => appRoot);
setTimeout(function() {
    var oldlog = global.console.log;
    global.console.log = function(text) {
        fetch('http://matt-dev:8000/' + encodeURI(text));
        oldlog.call(console, text);
    };

    console.log('started app');


    var app = require('./build/main.js');

    console.log('starting bridge');
    bridge.start();

    console.log('init app');
    tictactoe_android.core.init();
    console.log('after init app');
}, 1);
