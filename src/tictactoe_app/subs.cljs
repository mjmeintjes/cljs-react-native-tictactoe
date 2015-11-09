(ns tictactoe-app.subs
    (:require-macros [tictactoe-app.reframe-macros :refer [defsub]])
    (:require [re-frame.core :as re-frame]
              [cljs.test :refer-macros [deftest is]]))

(defsub board-state
  (get-in db [:board :state]))

(defsub current-player
  (get db :current-player))

;; * TESTS
(deftest should-subscribe
  (is (:board-state @re-frame.subs/key->fn)))

(deftest should-extract-board-state
  (is (= [:e :e :e] (board-state {:board {:state [:e :e :e]}}))))

(deftest defsub-macroexpand
  (println (macroexpand '(defsub board-state
                           (println db)
                           (get-in db [:board :state])))))
