(ns reagent-tictactoe.views
  (:require [re-frame.core :as rf]
            [reagent.core :as r :refer [atom]]
            [reactant.react :as react]))

(defn input-cmp [{:keys [repl-history on-save on-stop]}]
  (let [local-input (atom nil)
        stop #(do (reset! local-input "")
                  (r/flush)
                  (if on-stop (on-stop)))
        save #(let [v (-> @local-input str clojure.string/trim)]
                (if-not (empty? v) (on-save v))
                (stop))]
    (fn []
      [react/text-input
       {:on-submit-editing save
        :on-change-text #(do (reset! local-input %)
                             (r/flush))
        :value @local-input}])))

(defn root []
  (let [press-count  (rf/subscribe [:press-count])]
        ;;repl-history (rf/subscribe [:repl-history)]
    [react/view
     [react/touchable-highlight {:on-press #(rf/dispatch [:button-pressed])}
      [react/view {:style {:alignItems "center"}}
       [react/image {:source {:uri "https://raw.githubusercontent.com/cljsinfo/logo.cljs/master/cljs-white.png"}
                     :style {:width 50 :height 50}}]]]
     [react/text
      {:on-press #(rf/dispatch [:button-pressed])}
      (str "Hi from cljs and figwheel! state is:" @press-count)]
     ;;[react/text (str @repl-history)]
     [input-cmp {:repl-history []}]
     ]))

(defn main-panel []
  (let [name (rf/subscribe [:name])]
    (fn []
      [:div "Hello from " @name])))
