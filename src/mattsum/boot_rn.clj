(ns mattsum.boot-rn
  {:boot/export-tasks true}
  (:require [boot.core :as c :refer [deftask with-pre-wrap]]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [boot.file :as fl]
            [me.raynes.conch :refer [with-programs]]))


(defn split-line [line]
  (when-let [res (re-find #"[\"|'](.*?)[\"|'],\s(\[\'.*?\'\])" line)]
    [(res 1) (res 2)]))

(defn parse-deps [[path namespaces]]
  (when path
    (for [ns (s/split namespaces #",")]
      (let [clean-ns (s/replace ns #"[\[|\]|\']" "")
            path (if (.startsWith path "../")
                   (.replace path "../" "")
                   (str "goog/" path))]
        {:path path
         :ns (s/trim (str clean-ns ".js"))}))))

(defn get-deps [deps-file]
  (let [contents (slurp deps-file)
        lines (s/split contents #"\n")
        results (filter identity (map split-line lines))
        commands (map parse-deps results)]
    (flatten commands)))

(defn find-file [fileset path]
  (some->> path
           (c/tmp-get fileset)
           (c/tmp-file)))
(deftask link-goog-deps
  "Parses Google Closure deps files, and creates a link in the output node_modules directory to each file.

   This makes it possible for the React Native packager to pick up the dependencies when building the JavaScript Bundle allowing us to develop with :optimizations :none"
  [d deps-files DEPS #{str}  "A list of relative paths to deps files to parse"
   o output-dir OUT str  "The cljs :output-dir"]
  (let []
    (with-pre-wrap fileset
      (let [out-files (c/output-files fileset)
            tmp (c/tmp-dir!)
            deps-files (or deps-files ["cljs_deps.js" "goog/deps.js"])
            out-dir (str (or output-dir "main.out") "/")]

        (doseq [dep deps-files]
          (let [dep-maps (->> dep
                              (str out-dir)
                              (find-file fileset)
                              (get-deps)) ;; parse the dependencies from the file
                ]
            (doseq [{:keys [ns path]} dep-maps]
              (let [ns-file (io/file (str tmp "/node_modules") ns)
                    existing (some->> (str out-dir path)
                                      (find-file fileset))]
                (when (and existing (not (fl/exists? ns-file)))
                  (io/make-parents ns-file)
                  ;;(println "hard-link " existing " -> " ns-file)
                  (fl/hard-link existing ns-file))))))
        (-> fileset
            (c/add-resource tmp)
            c/commit!)))))

(use 'alex-and-georges.debug-repl)

(deftask replace-main
  "Replaces the main.js with a file that can be read by React Native's packager"
  [o output-dir OUT str  "The cljs :output-dir"]
  (let []
    (with-pre-wrap fileset
      (let [out-dir (str (or output-dir "main.out") "/")
            tmp (c/tmp-dir!)
            main-file (->> "main.js"
                           (find-file fileset))
            boot-main (->> main-file
                           slurp
                           (re-find #"(boot.cljs.\w+)\""))
            boot-main (get boot-main 1)
            out-file (io/file tmp "main.js")
            new-script (str "
var CLOSURE_UNCOMPILED_DEFINES = null;
require('./" out-dir "goog/base.js');
require('" boot-main "');
")]
        (spit out-file new-script)
        (-> fileset
            (c/add-resource tmp)
            c/commit!)))))

(deftask append-to-goog
  "Appends some javascript code to goog/base.js in order for React Native to work with Google Closure files"
  [o output-dir OUT str  "The cljs :output-dir"]
  (let []
    (with-pre-wrap fileset
      (let [out-dir (str (or output-dir "main.out") "/")
            tmp (c/tmp-dir!)
            base-file (->> "goog/base.js"
                           (str out-dir)
                           (find-file fileset))
            base-content (->> base-file
                              slurp)
            out-file (io/file (str tmp "/" out-dir "/goog") "base.js")
            new-script (str "
if (typeof global !== 'undefined') {
    global.goog = goog;
    var orig_require = goog.require;
    goog.require = function(name) {
        try {
            require (name);
        }
        catch (Error){}
        var parts = name.split('/');
        name = parts.slice(-1)[0];
        if (name.endsWith('.js')){
            name = name.slice(0, -3);
        };
        orig_require(name);
    };
}")
            out-content (str base-content "\n" new-script)]
        (doto out-file
          io/make-parents
          (spit out-content))
        (-> fileset
            (c/add-resource tmp)
            c/commit!)))))

(deftask react-native-devenv []
  (comp (link-goog-deps)
     (replace-main)
     (append-to-goog)))

(deftask start-rn-packager
  []
  (let []
    (c/with-post-wrap fileset
      (let [script "/home/matthys/projects/reagent-tictactoej/app/node_modules/react-native/packager/packager.sh"]
        (with-programs [sh]
          (sh script))
        ))))
