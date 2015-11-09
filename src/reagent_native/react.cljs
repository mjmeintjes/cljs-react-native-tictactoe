(ns reagent-native.react
  (:require-macros [reagent-native.macro :refer [adapt-react-classes]])
  (:require [reagent.core]))

(set! js/React (js/require "react-native/Libraries/react-native/react-native.js"))

(adapt-react-classes text
                     text-input
                     view
                     touchable-highlight
                     image)

(def app-registry (.-AppRegistry js/React))
