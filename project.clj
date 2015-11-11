(defproject reagent-tictactoe "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]
                 [reagent "0.5.1"]
                 [prismatic/schema "1.0.3"]
                 [re-frame "0.4.1" ]]

  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-figwheel "0.4.1" :exclusions [cider/cider-nrepl]]  ]
  :exclusions [cljsjs/react]
  :source-paths ["src"]

  :clean-targets ^{:protect false} ["build" "build_test" "test/js"]

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src"]

                        :figwheel {:on-jsload "tictactoe-android.core/on-js-reload"
                                   :websocket-host "192.168.1.85"
                                   :websocket-url "ws://192.168.1.85:3449/figwheel-ws"
                                   :heads-up-display false
                                   :debug false}

                        :compiler {:main tictactoe-android.core
                                   :asset-path "build/out"
                                   :output-to "build/reagent_tictactoe.js"
                                   :output-dir "build/out"
                                   :source-map-timestamp true}}
                       {:id "unittest"
                        :source-paths ["src" "test"]
                        :notify-command ["phantomjs" "test/unit-test.js" "test/unit-test.html"]
                        :compiler {:main test-runner
                                   :optimizations :none
                                   :asset-path "js"
                                   :pretty-print true
                                   :output-to "test/js/tests.js"
                                   :output-dir "test/js"
                                   :warnings {:single-segment-namespace false}}}

                       {:id "test"
                        :source-paths ["src"]
                        :compiler {:main tictactoe-android.core
                                   :asset-path "build_test/out"
                                   :output-to "build_test/reagent_tictactoe.js"
                                   :output-dir "build_test/out"
                                   :output-wrapper nil
                                   :recompile-dependents false
                                   :optimizations :simple}}]})
