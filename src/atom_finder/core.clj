(ns atom-finder.core
  ;(:require
  ; [atom-finder.classifier :refer :all]
  ; [atom-finder.count-subtrees :refer :all]
  ; [atom-finder.constants :refer :all]
  ; [atom-finder.util :refer :all]
  ; [atom-finder.atoms-in-dir :refer :all]
  ; [atom-finder.atom-patch :refer :all]
  ; [schema.core :as s]
  ; [clojure.pprint :refer [pprint]]
  ; [clojure.data.csv :as csv]
  ; [clojure.java.io :as io]
  ;          )
  (:gen-class)
  )

;(set! *warn-on-reflection* true)
(defn -main
  [& args]

  ;(->> (atom-patch/atoms-changed-all-commits gcc-repo atoms)
  ;     ;(map prn)
  ;     (take 10)
  ;     dorun)

  ; 48 hours
  ;(time (log-atoms-changed-all-commits "gcc-atom-removed-atoms_2017-05-25_0.edn" gcc-repo atoms))
)
