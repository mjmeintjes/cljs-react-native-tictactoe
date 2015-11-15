/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 */
'use strict';

var React = require('react-native');
var App = require('./build/main.js');
//var FigBridge = require('FigwheelBridge');

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
setTimeout(tictactoe_android.core.init, 1)

// For some reason, Reagent doesn't render on inital load unless this is async...
//setTimeout(FigBridge.start, 1);





