(ns tictactoe-app.handlers
    (:require [re-frame.core :as rf]
              [tictactoe-app.db :as db]))

(rf/register-handler
 :initialize-db
 (fn  [_ _]
   db/default-db))

(rf/register-handler
 :cell-clicked
 [rf/trim-v (rf/path :board :state)]
 (fn [board [pos player]]
   (rf/dispatch [:end-turn])
   (assoc board pos player)))

(rf/register-handler
 :end-turn
 (fn [data _]
   (println data)
   (assoc data :current-player (if (= (:current-player data) 1) 2 1))))
