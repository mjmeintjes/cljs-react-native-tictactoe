(ns tictactoe-android.views
  (:require-macros [tictactoe-app.reframe-macros :refer [with-subs]])
  (:require [re-frame.core :as rf]
            [tictactoe-app.subs :as subs]
            [reagent.core :as reag :refer [atom]]
            [reagent-native.react :as r]))

(defn board-comp
  "Iterates through board and returns a cell definition for each cell"
  [board player cell-func]
  (map-indexed (fn [idx state] [cell-func player board idx]) board))


(defn cell [player board pos]
  [r/touchable-highlight {:on-press #(rf/dispatch [:cell-clicked pos player])}
   [r/view
    [r/text (str "curr: " pos " - " (get board pos))]]])


(defn root []
  (let [board (rf/subscribe [:board-state])
        current-player (rf/subscribe [:current-player])]
    (fn []
      [r/view
       [r/text (str @board)]
       [r/text "HELLO"]
       (board-comp @board @current-player cell)])))
    ;;))

