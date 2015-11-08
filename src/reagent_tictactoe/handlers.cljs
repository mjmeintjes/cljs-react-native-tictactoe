(ns reagent-tictactoe.handlers
    (:require [re-frame.core :as re-frame]
              [reagent-tictactoe.db :as db]))

(re-frame/register-handler
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/register-handler
 :button-pressed
 (fn  [data _]
   (println "Button pressed")
   (update data :press-count inc)))

