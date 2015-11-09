(ns tictactoe-android.core
    (:require [reagent.core :as r]
              [re-frame.core :as rf]
              [tictactoe-app.handlers]
              [tictactoe-app.subs]
              [tictactoe-android.views :as views]))
(enable-console-print!)

(defn mount-root []
  (rf/dispatch-sync [:initialize-db])
  (r/render [views/root] 1))
  ;; ((fn render []
  ;;    (.requestAnimationFrame js/window render))))
;;(.registerRunnable react/app-registry "tictactoe" mount-root)

(defn ^:export init []
  (mount-root))
(.requestAnimationFrame js/window init)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  )
