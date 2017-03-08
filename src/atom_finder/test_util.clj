(ns atom-finder.test-util
  (:require [clojure.string :as str]
            [clojure.test :refer :all]
            [atom-finder.classifier :refer :all]
            [atom-finder.util :refer :all]
            )
  (:use     [clojure.pprint :only [pprint print-table]])
  (:import [org.eclipse.cdt.internal.core.parser.scanner ASTFileLocation]))

(defn find-lines
  "Find all lines marked with <true> in test file"
  [pat filepath]
  (let [regex (re-pattern pat)]
    (->>
     filepath
     slurp-lines
     (map #(re-find regex %))
     (keep-indexed #(if %2 (inc %1))))))

(def true-lines (partial find-lines "<true>"))

(defn test-atom-lines
  "Compare the results of an atom classifier with a regex search"
  [filename pat atom-finder]
  (let [filepath   (resource-path filename)
        expected   (find-lines pat filepath)
        lines  (->> filepath
                    tu
                    atom-finder
                    (map loc)
                    (map :line))]

    (is (empty? (sort (sym-diff (set expected) (set lines)))))))