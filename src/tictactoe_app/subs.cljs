(ns tictactoe-app.subs
  (:require-macros [reagent.ratom :refer [reaction]]
                   [tictactoe-app.reframe-macros :refer [defsub]])
    (:require [re-frame.core :as re-frame]
              [reagent.core :as r]
              [com.rpl.specter :as s]
              [cljs.test :refer-macros [deftest is]]))

;;(defn get-sorted-items
;;  [db]
;;  (let [items (reaction (:items @db))
;;        sort-attr (reaction (:sort-attr @db))]
;;    (reaction (sort-by @sort-attr @items))))
;;(register-sub :sorted-items get-sorted-items)
;;
;;(deftest sorted-items-test
;;  (let [db (r/atom {:items [{:val 4 :i 1} {:val 1 :i 2} {:val 3 :i 3}] :sort-attr :val})
;;        res (get-sorted-items db)]
;;    (is (= [{:val 1 :i 2} {:val 3 :i 3} {:val 4 :i 1}] @res))
;;    (swap! db assoc :sort-attr :i)
;;    (is (= [{:val 4 :i 1} {:val 1 :i 2} {:val 3 :i 3}]))))

(defn is-in-win-state
  [board win-state]
  (let [cell-values (mapv board win-state)
        in-win-state (apply = cell-values)
        first-value (first cell-values)
        not-empty (not= first-value 0)]
    (if (and in-win-state not-empty)
      first-value
      nil)))

(deftest is-in-win-state-should-return-winner-if-board-is-in-win-state
  (is ( = nil (is-in-win-state [0 0 0
                                0 1 0
                                0 0 1] [0 4 8])))
  (is ( = 1 (is-in-win-state [1 0 0
                              0 1 0
                              0 0 1] [0 4 8]))))

(defn is-tie
  [board]
  (let [not-empty (filter #(> % 0) board)
        is-tie (= (count not-empty) 9)]
    (when is-tie
      :tie)))

(defn is-winner
  [board]
  (let [win-states [[0 1 2]
                    [3 4 5]
                    [6 7 8]
                    [0 3 6]
                    [1 4 7]
                    [2 5 8]
                    [0 4 8]
                    [2 4 6]]
        win-state-checker (partial is-in-win-state board)
        winner (some win-state-checker win-states)]
    winner))

(defn board-game-result
  [board]
  (or (is-winner board) (is-tie board) :not-ended))

(defsub game-result
  (board-game-result (:board db)))

(deftest lookup
  (is (= [1 1 0] (mapv [0 0 0
                        1 1 0] [3 4 5]))))

(deftest game-result-should-calculate-ties
  (is (= :tie (game-result {:board [1 2 3 1 2 3 1 2 3]})))
  (is (= 1 (game-result {:board [1 1 1 0 0 0 0 0 0]})))
  (is (= :not-ended (game-result {:board [0 0 0 0 0 0 0 0 0]})))
  (is (= :not-ended (game-result {:board [1 1 0 2 2 0 1 1 0]})))
  (is (= 2 (game-result {:board [0 2 3
                                 1 2 3
                                 1 2 3]}))))

(defsub board-state
  (:board db))

(defsub current-player
  (get db :current-player))

;; * TESTS
(deftest should-subscribe
  (is (:board-state @re-frame.subs/key->fn)))

(deftest should-extract-board
  (is (= [:e :e :e] (board-state {:board [:e :e :e]}))))

;; (deftest defsub-macroexpand
  ;; (println (macroexpand '(defsub board-state
                           ;; (println db)
                           ;; (get-in db [:board :state])))))
