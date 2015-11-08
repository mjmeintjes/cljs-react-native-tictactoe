(ns reagent-tictactoe.core
    (:require [reagent.core :as r]
              [re-frame.core :as rf]
              [reagent-tictactoe.handlers]
              [reagent-tictactoe.subs]
              [reagent-tictactoe.views :as views]))
(enable-console-print!)

(defn ^:export init []
  (rf/dispatch-sync [:initialize-db])
  ((fn render []
     (.requestAnimationFrame js/window render))))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  )
(r/render [views/root] 1)
