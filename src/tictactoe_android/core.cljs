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

(defn ^:export init []
  (rf/dispatch-sync [:initialize-db])
  (mount-root))

(defn ^:export on-js-reload []
  (init))
