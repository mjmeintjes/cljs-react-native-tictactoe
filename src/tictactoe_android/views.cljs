(ns tictactoe-android.views
  (:require-macros [tictactoe-app.reframe-macros :refer [with-subs]])
  (:require [re-frame.core :as rf]
            [cljs.pprint :refer [pprint]]
            [tictactoe-app.subs :as subs]
            [reagent.core :as reag :refer [atom]]
            [reagent-native.react :as r]
            [com.rpl.specter :as s]
            [cljs.test :refer-macros [deftest is]]))

(def styles (r/create-style
             {:container {:flex 1
                          :justify-content "center"
                          :align-items "center"
                          :background-color "white"}
              :title {:font-family "Chalkduster"
                      :font-size 39
                      :margin-bottom 20}
              :board {:padding 5
                      :background-color "#47525d"
                      :border-radius 10}
              :row {:flex-direction "row"
                    :border-width 3
                    :border-color "black"
                    :border-style "solid"}
              :cell-view {:width 80
                          :height 80
                          :border-radius 5
                          :background-color "#7b8994"
                          :margin 5
                          :flex 1
                          :justify-content "center"
                          :align-items "center"}
              :cell-view-x {:background-color "#72d0eb"}
              :cell-view-o {:background-color "#7ebd26"}
              :cell-text {:border-radius 5
                          :font-size 50
                          :font-family "AvenirNext-Bold"}
              :cell-text-x {:color "#19a9e5"}
              :cell-text-o {:color "#b9dc2f"}
              :overlay {:position "absolute"
                        :top 0
                        :bottom 0
                        :left 0
                        :right 0
                        :background-color "rgba(221, 221, 221, 0.9)"
                        :flex 1
                        :flex-direction "column"
                        :justify-content "center"
                        :align-items "center"}
              :overlay-message {:font-size 40
                                :margin-bottom 20
                                :margin-left 20
                                :margin-right 20
                                :font-family "AvenirNext-DemiBold"
                                :text-align "center"}
              :new-game {:background-color "#887766"
                         :padding 20
                         :border-radius 5}
              :new-game-text {:color "white"
                              :font-size 20
                              :font-family "AvenirNext-DemiBold"}}))

(defn ensure-valid-hiccup
  [comp]
  (reag/render-to-static-markup comp))

(defn get-cell-state
  [state]
  (get [:empty :x :o] state))

(deftest should-get-correct-state
  (is (= :empty (get-cell-state 0)))
  (is (= :x (get-cell-state 1)))
  (is (= :o (get-cell-state 2))))


(defn get-cell-type-style
  [styles style-type state]
  (let [state-kw (get-cell-state state)
        key (keyword (str (name style-type) "-" (name state-kw)))]
    (styles key)))

(deftest get-cell-type-style-should-get-style-in-map
  (is (= "result" (get-cell-type-style {:a-x "result"} :a 1))))


(defn cell-text-styles [styles state]
  [(styles :cell-text) (get-cell-type-style styles :cell-text state)])

(defn cell-view-styles [styles state]
  [(styles :cell-view) (get-cell-type-style styles :cell-view state)])

(defn cell-text [state] (get ["" "t" "O"] state))

(defn game-end-message
  [result]
  ({:not-ended ""
    :tie "It's a tie"
    1 "X wins! Yeah!"
    2 "O wins!"} result))

(deftest game-end-message-should-return-string-message-given-game-result
  (is (= "X wins!" (game-end-message 1))))

(defn game-end-overlay
  [game-result]
  (let [result @game-result
        message (game-end-message result)]
    (if (= :not-ended result)
      [r/view]

      [r/view {:style (styles :overlay)}
       [r/text {:style (styles :overlay-message)} message]
       [r/touchable-highlight {:on-press #(rf/dispatch [:restart])
                               :underlay-color "transparent"
                               :active-opacity 0.5}
        [r/view {:style (styles :new-game)}
         [r/text {:style (styles :new-game-text)} "New Game"]]]])))


(deftest game-end-message-should-return-valid-hiccup-if-game-not-ended
  (ensure-valid-hiccup (game-end-overlay (atom :not-ended))))

(deftest game-end-message-should-return-valid-hiccup-if-game-ended
  (ensure-valid-hiccup (game-end-overlay (atom 1))))

(deftest game-end-message-should-return-empty-view-if-game-not-ended
  (is (= [:mock-view] (game-end-overlay (atom :not-ended)))))

(defn cell
  [{:keys [col state on-press]}]
  [r/touchable-highlight {:on-press on-press :key (str col) :underlay-color "transparent" :active-opacity 0.5}
   [r/view {:style (cell-view-styles styles state)}
    [r/text {:style (cell-text-styles styles state)}
     (cell-text state)]]])

(deftest cell-should-return-valid-hiccup
  (ensure-valid-hiccup (cell {:col 1 :state 1})))

(deftest cell-should-display-cell-text-based-on-state
  (is (= "" (last (flatten (cell {:state 0})))))
  (is (= "X" (last (flatten (cell {:state 1})))))
  (is (= "O" (last (flatten (cell {:state 2}))))))


(defn create-cell-component
  [row col state]
  [cell {:col col
         :key col
         :state state
         :on-press #(rf/dispatch [:cell-pressed row col])}])

(deftest create-cell-component-should-generate-valid-hiccup
  (ensure-valid-hiccup (create-cell-component 0 1 2)))

(defn create-row-component
  [idx row]
  (into [r/view {:key (str "row-" idx)
                 :style (styles :row)}]
         (map-indexed (partial create-cell-component idx) row)))

(deftest create-row-component-should-return-valid-hiccup
  (ensure-valid-hiccup (create-row-component 1 [0 0 0])))

(defn create-board
  [grid]
  (let [rows (partition 3 grid)]
    (into [r/view {:style (styles :board)}]
          (map-indexed create-row-component rows))))

(deftest create-board-should-return-valid-hiccup
  (let [board (create-board [0 0 0])]
    (ensure-valid-hiccup board)))

;; (deftest create-board-should-create-board-from-grid
  ;; (let [comps (create-board [0 0 0
                             ;; 0 0 0
                             ;; 0 0 0])]
    ;; (println comps)))

(defn tic-tac-toe-app
  []
  (let [grid (rf/subscribe [:board-state])
        game-result (rf/subscribe [:game-result])]
    (fn []
      (let [row-components (create-board @grid)]
        [r/view {:style (styles :container)}
         [r/text {:style (styles :title)} "RE-FRAME T3"]
          row-components
         [game-end-overlay game-result]]))))

(deftest tic-tac-toe-app-should-return-func
  (ensure-valid-hiccup ((tic-tac-toe-app))))

(defn root []
  [tic-tac-toe-app])

