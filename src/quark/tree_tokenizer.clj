;; Convert a C/C++/Header file into an edn representation containing the
;; structure and (partial) type information of the AST

(ns quark.tree-tokenizer
  (:require [atom-finder.util :refer :all]
            [atom-finder.constants :refer :all]
            [schema.core :as s]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as str]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [swiss.arrows :refer :all]
            [clj-cdt.clj-cdt :refer :all]
            [clj-cdt.writer-util :refer :all]
            )
  (:import
   [org.eclipse.cdt.core.dom.ast IASTNode IASTBinaryExpression
    IASTExpression IASTStatement IASTTranslationUnit IASTName
    IASTExpressionList IASTExpressionStatement IASTForStatement
    IASTPreprocessorMacroDefinition IASTIfStatement IASTUnaryExpression
    IProblemBinding IProblemType]
   [org.eclipse.cdt.internal.core.dom.parser.cpp CPPASTTranslationUnit]
   [org.eclipse.cdt.internal.core.dom.rewrite.astwriter ASTWriter]
   [org.eclipse.cdt.internal.core.parser.scanner ASTMacroDefinition]
   ))

(defn expr-typename
  [node]
  (log-err (str "Exception getting expression type for [file node] "
                [(filename node) (tree-path node)]) "---exception---"
           (let [expr-type (-> node .getExpressionType)]
             (condp instance? expr-type
               IProblemBinding "problem-binding"
               IProblemType "problem-type"
               (str expr-type)))))

(defmulti to-poco "Convert IASTNode node to plain-old-clojure-object's" class)

(s/defmethod to-poco :default [node] [(typename node)])

(s/defmethod to-poco IASTExpression [node] [(typename node) (expr-typename node)])
(s/defmethod to-poco IASTUnaryExpression [node] [(typename node) (expr-typename node) (->> node expr-operator :name)])
(s/defmethod to-poco IASTBinaryExpression [node] [(typename node) (expr-typename node) (->> node expr-operator :name)])
(s/defmethod to-poco IASTName [node] [(typename node) (->> node node-name)])

(defn to-edn
  "Serialize AST node into an edn list"
  [node]
  (let [poco (to-poco node)]
    (if (leaf? node)
      poco
      (cons poco (map to-edn (children node))))))

'((->> "x" pap parse-frag expr-typename pprint))
'((->> "f(x)" pap parse-frag expr-typename pprint))
'((->> "1" pap parse-frag to-edn pprint))
'((->> "{int x; x;}" parse-frag to-edn pprint))
'((->> "\"abc\" + 2" parse-frag to-edn pprint))
'((->> "if(1) 'b';" parse-frag to-edn pprint))

(defn src-dir-to-edn
  [unexpanded-src-path unexpanded-out-path]
  (doseq [:let [src-path (expand-home unexpanded-src-path)
                out-path (expand-home unexpanded-out-path)]
          src-file (c-files src-path)
          :let [src-filename (.getAbsolutePath src-file)
                out-filename (str (str/replace src-filename src-path out-path) ".edn")]]
    (clojure.java.io/make-parents out-filename)
    (->> src-filename parse-file to-edn (spit out-filename))))

'((time-mins (src-dir-to-edn linux-path "tmp/src-to-edn/linux4")))

;(defn all-child-paths
;  "given a directory path, find all relative paths under it"
;  []
;  (let [{true dirs false files}
;        (->> "~/nyu/confusion/atom-finder/" expand-home files-in-dir
;             (group-by (memfn isDirectory)))])

(defn split-path [path] (str/split path #"/"))

(defn all-child-paths
  "given a directory path, find all relative paths under it"
  [path]
  (-<>> path files-in-dir (map (memfn getCanonicalPath))
        ;(map #(str/replace % (re-pattern (str path "/?")) "")) ;; strip leading paths
        ))

(defn included-files
  "extract the files included from this file"
  [filename]
  (->> filename
       slurp-lines
       (map (partial re-find #"#include *[\"<]([^\">]*)[\">]"))
       (remove nil?)
       (map last)))

(def include-files (->> "~/opt/src/mongo/src/mongo/base/parse_number.cpp" expand-home included-files))

(defn infer-include-paths
  [all-paths include-paths]
  (->> include-paths
       (filter #(.contains % "/"))
       (mapcat (fn [include-path]
                 (->> all-paths
                      (map (fn [child-path] [child-path (str/index-of child-path include-path)]))
                      (remove (comp nil? last))
                      (map (fn [[path idx]] (subs path 0 idx)))
                      )))
       distinct))

(def stdlib-paths ["/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include/c++/4.2.1" "/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include"])

(defn parent-dir [path] (->> path java.io.File. .getParent))

(defn parse-file-with-proj-includes
  "infer which project-level include paths might be useful then parse with them"
  [all-paths file]
  (let [include-paths (infer-include-paths all-paths include-files)]
    (parse-file file {:include-dirs (concat [(parent-dir file)] stdlib-paths
                                            include-paths
                                            )})))

(def mongo-files (->> "~/opt/src/mongo" expand-home all-child-paths))

(->> "/Users/dgopstein/opt/src/mongo/src/mongo/base/secure_allocator.cpp"
     expand-home
     (parse-file-with-proj-includes mongo-files)
     ;parse-file
     flatten-tree
     (remove from-include?)
     (filter (partial instance? IASTExpression))
     (map expr-typename)
     frequencies
     (group-by (comp not #(str/starts-with? % "problem-") first))
     (map-values (%->> (map last) sum))
     pprint
     time-mins)


(defn src-csv-to-edn
  [csv-path column-name]
  (with-open [reader (io/reader csv-path)
              writer (io/writer (str csv-path ".edn"))]
    (let [[header & csv-data] (csv/read-csv reader)
          code-idx (.indexOf header column-name)
          edn-data (map #(update-in % [code-idx] (comp to-edn parse-source)) csv-data)]
     (csv/write-csv writer (cons header edn-data))
      )))


'((time-mins (src-csv-to-edn "tmp/context_study_code.csv" "Code")))