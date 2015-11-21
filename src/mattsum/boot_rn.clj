(ns mattsum.boot-rn
  {:boot/export-tasks true}
  (:require [boot.core :as c :refer [deftask with-pre-wrap]]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.pprint :refer [pprint]]
            [boot.file :as fl]
            [boot.util :as util]
            [me.raynes.conch :refer [with-programs]]))


(defn split-line [line]
  (when-let [res (re-find #"[\"|'](.*?)[\"|'],\s(\[\'.*?\'\])" line)]
    [(res 1) (res 2)]))

(defn parse-deps [[path namespaces]]
  (when path
    (for [ns (s/split namespaces #",")]
      (let [clean-ns (s/replace ns #"[\[|\]|\']" "")
            path     (if (.startsWith path "../")
                       (.replace path "../" "")
                       (str "goog/" path))]
        {:path path
         :ns (s/trim (str clean-ns ".js"))}))))

(defn get-deps [deps-file]
  (let [contents (slurp deps-file)
        lines    (s/split contents #"\n")
        results  (filter identity (map split-line lines))
        commands (map parse-deps results)]
    (flatten commands)))

(defn find-file [fileset path]
  (some->> path
           (c/tmp-get fileset)
           (c/tmp-file)))

(defn add-provides-module-metadata
  "Adds react's @providesModule metadata to javascript file"
  [source-file target-file module-name]
  (let [content (slurp source-file)
        new-content (str "/* \n * @providesModule " (.replace module-name ".js" "") "\n */\n" content)]
    (spit target-file new-content)))

(use 'alex-and-georges.debug-repl)

(defn setup-links-for-dependency-map
  [dependency-map fileset tmp-dir src-dir]

  (doseq [{:keys [ns path]} dependency-map]
    (let [target-file (io/file (str tmp-dir "/node_modules") ns)
          source-file (some->> (str src-dir path)
                               (find-file fileset))
          ]
      (when source-file
        (io/make-parents target-file)
        (add-provides-module-metadata source-file target-file ns)))))

(deftask link-goog-deps
  "Parses Google Closure deps files, and creates a link in the output node_modules directory to each file.

   This makes it possible for the React Native packager to pick up the dependencies when building the JavaScript Bundle allowing us to develop with :optimizations :none"
  [d deps-files DEPS #{str}  "A list of relative paths to deps files to parse"
   o cljs-dir OUT str  "The cljs :output-dir"]
  (let [previous-files (atom nil)
        output-dir (c/tmp-dir!) ; Create the output dir in outer context allows us to cache the compilation, which means we don't have to re-parse each file
        ]
    (with-pre-wrap fileset
      (let [get-hash-diff #(c/fileset-diff @previous-files % :hash)

            new-files     (->> fileset
                               get-hash-diff)
            deps-files    (or deps-files ["cljs_deps.js" "goog/deps.js"])
            src-dir       (str (or cljs-dir "main.out") "/")]
        (reset! previous-files fileset)

        (util/info "Compiling {cljs-deps}... %d changed files\n" (count new-files) )
        (doseq [dep-file deps-files]
          (let [dep-maps (->> dep-file
                              (str src-dir)
                              (find-file fileset)
                              (get-deps)) ;; parse the dependencies from the file
                ]
            (setup-links-for-dependency-map dep-maps new-files output-dir src-dir)))
        (-> fileset
            (c/add-resource output-dir)
            c/commit!)))))



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
    goog.provide = function(name) {
        if (goog.isProvided_(name)) {
            return; //don't throw an error when called multiple times, because it is going to be called multiple times in from react-native
        }
        delete goog.implicitNamespaces_[name];

        var namespace = name;
        while ((namespace = namespace.substring(0, namespace.lastIndexOf('.')))) {
            if (goog.getObjectByName(namespace)) {
                break;
            }
            goog.implicitNamespaces_[namespace] = true;
        }
        goog.exportPath_(name);
    };
    goog.require = function(name) {
        try {
            require(name);
        }
        catch (e) {
            console.log(e);
        }
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
