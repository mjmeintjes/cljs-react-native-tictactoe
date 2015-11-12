(ns reagent-native.react
  (:require-macros [reagent-native.macro :refer [adapt-react-classes]])
  (:require [reagent.core]
            [cljs.test :refer-macros [deftest is]]
            [reagent.impl.util :refer [dash-to-camel]]
            [reagent.impl.component :as ru]))

;; Setup React Native javascript object, and if it fails, set it to an empty object to enable testing
(try
  (set! js/React (js/require "react-native/Libraries/react-native/react-native.js"))
  (catch js/ReferenceError e
    (aset js/React "StyleSheet" {})
    (aset js/React "StyleSheet" "create" clj->js)
    ))

(adapt-react-classes text
                     text-input
                     view
                     touchable-highlight
                     image)


(def app-registry (.-AppRegistry js/React))

(defn create-style
  [s]
  (let [s1 (reduce #(assoc %1 (%2 0) (ru/camelify-map-keys (%2 1))) {} s)
        styles (js->clj (.create (.-StyleSheet js/React) (clj->js s1)))]
    (fn get-style [keyword]
      (styles (str (dash-to-camel (name keyword)))))))

(deftest create-style-should-create-camelcase-map-keys
  (let [styles (create-style {:main-test {:border-color "green"}})]
    (is (= {"borderColor" "green"} (styles :main-test)))))
