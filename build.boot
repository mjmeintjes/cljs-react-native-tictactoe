(set-env!
 :source-paths   #{"src" "react-support"}
 :resource-paths #{"html"}
 :exclusions ['cljsjs/react]
 :dependencies '[
                 [adzerk/boot-cljs               "1.7.170-3"       :scope  "test"]
                 [adzerk/boot-cljs-repl          "0.3.0"           :scope  "test"]
                 [adzerk/boot-reload             "0.4.2"           :scope  "test"]
                 [pandeiro/boot-http             "0.7.1-SNAPSHOT"  :scope  "test"]
                 [crisptrutski/boot-cljs-test    "0.2.0-SNAPSHOT"  :scope  "test"]
                 [com.cemerick/piggieback        "0.2.1"           :scope  "test"]
                 [weasel                         "0.7.0"           :scope  "test"]
                 [org.clojure/tools.nrepl        "0.2.12"          :scope  "test"]
                 [org.clojars.gjahad/debug-repl  "0.3.3"           :scope  "test"]
                 [org.clojure/clojure            "1.7.0"]
                 [org.clojure/clojurescript      "1.7.145"]
                 [com.rpl/specter                "0.8.0"]
                 [reagent                        "0.5.1"]
                 [prismatic/schema               "1.0.3"]
                 [re-frame                       "0.4.1"]
                 [mathias/boot-restart           "0.0.2"]
                 ])

(require
 '[adzerk.boot-cljs             :refer  [cljs]]
 '[adzerk.boot-cljs-repl        :refer  [cljs-repl  start-repl]]
 '[adzerk.boot-reload           :refer  [reload]]
 '[crisptrutski.boot-cljs-test  :refer  [test-cljs]]
 '[pandeiro.boot-http           :refer  [serve]]
 '[mathias.boot-restart         :refer  [restart]]
 '[boot.core                    :as     b]
 '[clojure.string               :as     s]
 '[mattsum.boot-rn              :as     rn]
 )

;;; This prevents a name collision WARNING between the test task and
;;; clojure.core/test, a function that nobody really uses or cares
;;; about.
(ns-unmap 'boot.user 'test)

(deftask testing []
  (set-env! :target-path "tests-target"
            :exclusions []
            :source-paths #(disj % "react-support")
            :dependencies #(conj % '[cljsjs/react "0.14.0-1"]))

  identity)

(deftask auto-test []
  (comp (testing)
     (watch)
     (speak)
     (test-cljs :js-env :phantom
                :compiler {:verbose true}
                :namespaces ['tictactoe-android.views
                             'tictactoe-app.handlers
                             'tictactoe-app.db
                             'reagent-native.react
                             'tictactoe-app.subs])))
(deftask dev []
  (set-env! :target-path "app/build")
  (comp (watch)
     (reload :on-jsload 'tictactoe-android.core/on-js-reload
             :port 8079
             :ws-host "matt-dev")
     (cljs-repl :ws-host "matt-dev"
                :ip "0.0.0.0")
     (cljs :source-map true
           :optimizations :none)
     ))

(deftask fast-build []
  (set-env! :target-path "app/build")
  (comp (serve :dir "app/build/" :port 8082)
     (watch)
     (cljs-repl :ws-host "matt-dev"
                :ip "0.0.0.0")

     (cljs :source-map true
           :optimizations :none)
     (rn/react-native-devenv)))

(deftask build []
  (set-env! :target-path "app/build")
  (comp (cljs :optimizations :advanced)))
