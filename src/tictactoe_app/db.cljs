(ns tictactoe-app.db
  (:require [schema.core :as s :include-macros true]
            [cljs.test :refer-macros [deftest testing is]]))

(def CellState (s/enum 0 1 2))

(def GameBoard
  "A schema for tictactoe game board"
  {:state [CellState]})

(def Db
  "A schema for tictactoe game"
  {:current-player (s/enum 1 2)
   :board [CellState]})

(def default-db
  {:current-player 1
   :board [0 1 0
           0 2 0
           0 0 0]})


(deftest valid-default
  (s/validate Db default-db))

(deftest example-gameboard
  (s/validate GameBoard {:state [0 0 0
                                 1 0 1
                                 0 2 0]}))
