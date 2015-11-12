(ns tictactoe-app.subs
  (:require-macros [reagent.ratom :refer [reaction]]
                   [tictactoe-app.reframe-macros :refer [defsub]])
    (:require [re-frame.core :as re-frame]
              [reagent.core :as r]
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
(defsub game-result
  :not-ended)

(defsub board-state
  (get-in db [:board]))

(defsub current-player
  (get db :current-player))

;; * TESTS
(deftest should-subscribe
  (is (:board-state @re-frame.subs/key->fn)))

(deftest should-extract-board-state
  (is (= [:e :e :e] (board-state {:board [:e :e :e]}))))

;; (deftest defsub-macroexpand
  ;; (println (macroexpand '(defsub board-state
                           ;; (println db)
                           ;; (get-in db [:board :state])))))
