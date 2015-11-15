(ns tictactoe-app.handlers
  (:require [re-frame.core :as rf]
            [cljs.test :refer-macros [deftest is]]
            [com.rpl.specter :as s]
            [tictactoe-app.db :as db]
            [tictactoe-app.subs :as subs]))


(rf/register-handler
 :initialize-db
 (fn  [db _]
   (if (:board db)
     db
     db/default-db)))

(rf/register-handler
 :restart
 (fn  [db _]
   db/default-db))

(defn update-cell-in-db
  [db row col]
  (let [player (:current-player db)
        cell (+ col (* row 3))
        board (:board db)
        current-state (get board cell)]
    (println )
    (if (and (= current-state 0) (= (subs/board-game-result board) :not-ended))
      (->> db
           (s/setval [:board (s/keypath cell)] player)
           (s/setval [:current-player] (if (= player 1) 2 1)))
      db)))

(deftest transform-tests
  (is (= [0 1 0] (s/setval [(s/keypath 1)] 1 [0 0 0]))))

(rf/register-handler
 :cell-pressed
 [rf/trim-v]
 (fn [db [row col]]
   (update-cell-in-db db row col)))

(deftest update-cell-should-set-state-to-current-player
  (let [board [0 0 0
               0 1 2
               0 0 0]]
    (is (= {:current-player 2
            :board [0 0 0
                    0 1 2
                    0 1 0]} (update-cell-in-db {:board board :current-player 1} 2 1)))
    (is(= {:current-player 1
            :board [0 0 0
                    0 1 2
                    0 2 0]} (update-cell-in-db {:board board :current-player 2} 2 1)))))

(rf/register-handler
 :end-turn
 (fn [data _]
   (println data)
   (assoc data :current-player (if (= (:current-player data) 1) 2 1))))
