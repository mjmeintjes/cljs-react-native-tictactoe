(defproject reagent-tictactoe "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]
                 [reagent "0.5.1"]
                 [re-frame "0.4.1" ]]

  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-figwheel "0.4.1" :exclusions [cider/cider-nrepl]]  ]
  :exclusions [cljsjs/react]
  :source-paths ["src"]

  :clean-targets ^{:protect false} ["build" "build_test"]

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src"]

                        :figwheel {:on-jsload "reagent-tictactoe.core/on-js-reload"
                                   :websocket-host "matt-dev"
                                   :websocket-url "ws://matt-dev:3449/figwheel-ws"
                                   :heads-up-display false
                                   :debug false}

                        :compiler {:main reagent-tictactoe.core
                                   :asset-path "build/out"
                                   :output-to "build/reagent_tictactoe.js"
                                   :output-dir "build/out"
                                   :source-map-timestamp true}}

                       {:id "test"
                        :source-paths ["src"]
                        :compiler {:main reagent-tictactoe.core
                                   :asset-path "build_test/out"
                                   :output-to "build_test/reagent_tictactoe.js"
                                   :output-dir "build_test/out"
                                   :output-wrapper nil
                                   :optimizations :simple}}]})
