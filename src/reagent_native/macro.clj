(ns reagent-native.macro
  (:require [clojure.string :as string]))

(defn snake-case->camel-case [^String input-str]
  (string/replace input-str
                  #"-(\w)"
                  (fn [[match captured]]
                    (string/upper-case captured))))

(defn generate-binding [clj-name]
  (let [js-name (snake-case->camel-case (string/capitalize (str clj-name)))
        attr-symbol (symbol (str ".-" js-name))]
    `(def ~clj-name
       (if (~attr-symbol js/React)
         (reagent.core/adapt-react-class (~attr-symbol js/React))
         (keyword (str "mock-" '~clj-name))))))

(defmacro adapt-react-classes [& list]
  (let [generated-bindings (map generate-binding list)]
    `(do ~@generated-bindings)))

#_(macroexpand-1 '(adapt-react-classes text
                                       view
                                       app-registry))
