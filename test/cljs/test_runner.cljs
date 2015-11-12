(ns test-runner
  (:require
   [cljs.test :as test :refer-macros [run-tests] :refer [report]]
   [tictactoe-app.db]
   [tictactoe-app.handlers]
   [tictactoe-android.views]
   [reagent-native.react]
   [tictactoe-app.subs]))

(enable-console-print!)

(defmethod report [::test/default :summary] [m]
  (println "\nRan" (:test m) "tests containing"
           (+ (:pass m) (:fail m) (:error m)) "assertions.")
  (println (:fail m) "failures," (:error m) "errors.")
  (aset js/window "test-failures" (+ (:fail m) (:error m))))

(defn runner []
  (test/run-tests
   (test/empty-env ::test/default)
   'tictactoe-android.views
   'tictactoe-app.handlers
   'tictactoe-app.db
   'reagent-native.react
   'tictactoe-app.subs))
