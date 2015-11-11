(ns tictactoe-android.views
  (:require-macros [tictactoe-app.reframe-macros :refer [with-subs]])
  (:require [re-frame.core :as rf]
            [cljs.pprint :refer [pprint]]
            [tictactoe-app.subs :as subs]
            [reagent.core :as reag :refer [atom]]
            [reagent-native.react :as r]
            [cljs.test :refer-macros [deftest is]]))

(defn get-cell-state
  [state]
  (get [:empty :x :o] state))

(deftest should-get-correct-state
  (is (= :empty (get-cell-state 0)))
  (is (= :x (get-cell-state 1)))
  (is (= :o (get-cell-state 2))))

(defn get-cell-type-style
  [styles style-type state]
  (get-in styles [style-type state] (get-cell-state state)))

(deftest get-cell-type-style-should-get-style-in-map
  (is (= 1 (get-cell-type-style {:a {:b 1}} :a :b))))

(defn cell-text-styles [styles state]
  [(:cell-text styles) (get-cell-type-style styles :cell-text state)])

(defn cell-view-styles [styles state]
  [(:cell-view styles) (get-cell-type-style styles :cell-view state)])

(defn cell-text [state] (get ["" "X" "O"] state))

(defn game-end-message
  [result]
  (get {:not-ended ""
        :tie "It's a tie"
        :x "X wins!"
        :o "O wins!"} result))

(defn game-end-overlay
  [game-result]
  (let [result @game-result
        message (game-end-message result)]
    (if (= :not-ended result)
      [r/view]

      [r/view {:style (:overlay styles)}
       [r/text {:style (:overlay-message styles)} message]
       [r/touchable-highlight {:on-press #(rf/dispatch [:restart-game])
                               :underlay-color "transparent"
                               :active-opacity 0.5}
        [r/view {:style (:new-game styles)}
         [r/text {:style (:new-game-text styles)} "New Game"]]]])))

(defn cell
  [{:keys [col state on-press]}]
  [r/touchable-highlight {:on-press on-press :key (str col) :underlay-color "transparent" :active-opacity 0.5}
   [r/view {:style (cell-view-styles styles state)}
    [r/text {:style (cell-text-styles styles state)}
     (cell-text state)]]])

(deftest cell-should-return-hiccup-tree
  (pprint (cell {:col 1
                  :state 2
                  :on-press "on-press-func"}))
  (is (= 1 (count (cell)))))

(defn create-cell-component
  [row col state]
  [cell {:col col
         :state state
         :on-press #(rf/dispatch [:cell-pressed row col])}])

(defn create-row-component
  [idx row]
  [r/view {:key (str "row-" idx)
           :style (:row styles)}
   (map-indexed (partial create-cell-component idx) row)])

(defn create-board
  [grid]
  (let [rows (partition 3 grid)]
    (map-indexed create-row-component rows)))

(defn tic-tac-toe-app
  []
  (let [grid (rf/subscribe [:board-state])
        game-result (rf/subscribe [:game-result])]
    (fn []
      (let [row-components (map-indexed create-board @grid)]
        [r/view {:style (:container styles)}
         [r/text {:style (:title styles)} "EXTREME CLJS T3"]
         [r/view {:style (:board styles)}
          [row-components]]
         [game-end-overlay game-result]]))))

(defn root []
  [tic-tac-toe-app])

