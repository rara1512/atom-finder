;; find parens which are not technically necessary

(ns atom-finder.questions.superfluous-parens
  (:require
   [atom-finder.constants :refer :all]
   [atom-finder.classifier :refer :all]
   [atom-finder.util :refer :all]
   [atom-finder.tree-diff :refer :all]
   [atom-finder.questions.question-util :refer :all]
   [clj-cdt.clj-cdt :refer :all]
   [clj-cdt.expr-operator :refer :all]
   [clj-cdt.writer-util :refer :all]
   [clj-cdt.modify-ast :refer :all]
   [clojure.pprint :refer [pprint]]
   [clojure.string :as string]
   [swiss.arrows :refer :all]
   [schema.core :as s])
  (:import [atom_finder.classifier Atom])
   )

(s/defn superfluous-parens? [node :- (s/pred paren-node?)]
  (let [mom (parent node)
        kid (child node)
        parenless-mom (replace-expr mom node kid)
        reparsed-parenless-mom (->> parenless-mom write-ast parse-expr)
        ]

    (tree=by (juxt class expr-operator) parenless-mom reparsed-parenless-mom)
  ))

(defn find-parens
  [root]
  (let [paren-types
        [(Atom. :parens paren-node? (default-finder paren-node?))
         (Atom. :superfluous-parens superfluous-parens?
                (default-finder #(when (paren-node? %) (superfluous-parens? %))))
         (atom-lookup :operator-precedence)]]
  (find-atoms paren-types root)))

(defn child-or-nil
  "If this node has one child return it, otherwise return nil"
  [node]
  (let [[kid & others] (children node)]
    (when (empty? others)
      kid)))

(defn find-all-parens-in-project
  [edn-file]
  (println (str (now)))
  (->> atom-finder-corpus-path
       (pmap-dir-c-files
        (fn [file]
          (merge
           {:file (atom-finder-relative-path file)}
           (->> file parse-file find-parens
                (map-values (partial map (fn [node]
                                           (merge {:parent-type (-> node parent opname-or-typename)
                                                   :node-type (-> node opname-or-typename)
                                                   :child-type (some-> node child-or-nil opname-or-typename)}
                                                  (dissoc (loc node) :length :start-line)
                                                  )))))
           )))
       (map prn)
       dorun
       (log-to edn-file)
       time-mins
       ))

(defn main-superfluous-parens
  []
  (let [edn-file "tmp/all-parens_2018-10-29_02_child.edn"
        csv-file "src/analysis/data/all-parens_2018-10-29_02_child.csv"
        ]
    (find-all-parens-in-project edn-file)
    ;(summarize-all-nodes edn-file csv-file)
  ))
