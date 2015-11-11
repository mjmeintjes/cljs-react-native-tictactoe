(ns reagent-native.react
  (:require-macros [reagent-native.macro :refer [adapt-react-classes]])
  (:require [reagent.core]))

;; Setup React Native javascript object, and if it fails, set it to an empty object to enable testing
(try
  (set! js/React (js/require "react-native/Libraries/react-native/react-native.js"))
  (catch js/ReferenceError e
    (set! js/React {:mock true})
    (aset js/React "View" "view")))

(adapt-react-classes text
                     text-input
                     view
                     touchable-highlight
                     image)


(def app-registry (.-AppRegistry js/React))
