(ns atom-finder.atom-stats
  (:require
   [atom-finder.util :refer :all]
   [atom-finder.constants :refer :all]
   [clojure.pprint :refer [pprint]]
   [schema.core :as s]
   )
  )

(s/defn ba-counts
  [srcs atom]
  {:atom-count-before (->> srcs :ast-before ((:finder atom)) count)
   :atom-count-after  (->> srcs :ast-after  ((:finder atom)) count)})

(defn source-size-before-after [srcs atom]
  {:source-chars-before (-> srcs :source-before count)
   :source-chars-after  (-> srcs :source-after count)})

(defn atom-stats [] {
   :atom-counts-before-after ba-counts
   :source-size-before-after source-size-before-after
   })