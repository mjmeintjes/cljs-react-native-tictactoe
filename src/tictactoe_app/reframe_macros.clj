(ns tictactoe-app.reframe-macros)

(defmacro defsub
  [subname & fn]
  `(do
     (defn ~subname
       [~'db]
       ~@fn)
     (re-frame.core/register-sub (keyword '~subname)
                                 (fn [~'db] (reagent.ratom.make-reaction (fn [] (~subname @~'db)))))))


;; * Registering components with subscribes
(defn- to-sub
  [[binding sub]]
  `[~binding (re-frame.core/subscribe ~sub)])

(defn- to-deref
  [binding]
  `[~binding (deref ~binding)])

(defmacro with-subs
  [bindings & body]
  `(let [~@(apply concat (map to-sub (partition 2 bindings)))]
     (fn []
       (let [~@(apply concat (map to-deref (take-nth 2 bindings)))]
         ~@body))))



