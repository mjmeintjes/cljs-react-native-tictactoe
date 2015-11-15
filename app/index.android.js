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

AppRegistry.registerComponent('tictactoe', () => appRoot);

//TODO: This is a terrible hack, need to find better way of switching between the 2 entry points
try {
    var app = require('./build/main.js');
    setTimeout(tictactoe_android.core.init, 1);
}
catch (e){
    if (e.message.indexOf("find variable: document") > -1 || e.message.indexOf("document is not defined") > -1){
        setTimeout(function() {
            try {
                bridge.start();
                tictactoe_android.core.init();
            }
            catch (e) {
                if (e.message.indexOf('find variable: goog')){
                    throw new Error("Only works if Chrome Debugging is enabled") ;
                }
                throw e;
            }
        }, 1);
    } else {
        throw e;
    }
}





