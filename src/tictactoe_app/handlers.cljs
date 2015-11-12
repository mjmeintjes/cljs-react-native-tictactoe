(ns tictactoe-app.handlers
  (:require [re-frame.core :as rf]
            [cljs.test :refer-macros [deftest is]]
            [com.rpl.specter :as s]
            [tictactoe-app.db :as db]))

(rf/register-handler
 :initialize-db
 (fn  [_ _]
   db/default-db))

(defn update-cell
  [board current-player row col]
  (let [cell (+ col (* row 3))
        current-state (get board cell)]
    (if (= current-state 0)
      (assoc board cell current-player)
      board)))

(defn update-cell-in-db
  [db row col]
  (let [player (:current-player db)]
    (assoc db :board (update-cell (:board db) player row col))))

(rf/register-handler
 :cell-pressed
 [rf/trim-v]
 (fn [db [row col]]
   (let [new-db (update-cell-in-db db row col)]
     (if (= db new-db)
       db
       (do (rf/dispatch [:end-turn])
           new-db)))))

(deftest update-cell-should-set-state-to-current-player
  (let [board [0 0 0
               0 1 2
               0 0 0]]
    (is (= [0 0 0
            1 1 2
            0 0 0] (update-cell board 1 1 0)))
    (is (= [0 0 0
            0 1 2
            0 2 0] (:board (update-cell-in-db {:board board :current-player 2} 2 1))))))

(rf/register-handler
 :end-turn
 (fn [data _]
   (println data)
   (assoc data :current-player (if (= (:current-player data) 1) 2 1))))
