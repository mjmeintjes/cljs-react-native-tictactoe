(ns reagent-tictactoe.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(re-frame/register-sub
 :press-count
 (fn [db]
   (reaction (:press-count @db))))

(re-frame/register-sub
 :repl-history
 (fn [db]
   (reaction (:repl-history @db))))

