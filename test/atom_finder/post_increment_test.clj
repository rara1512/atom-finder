(ns atom-finder.post-increment-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [atom-finder.util :refer :all]
            [atom-finder.classifier :refer :all]
            ))

(use-fixtures :once schema.test/validate-schemas)

(defn true-lines
  "Find all lines marked with <true> in test file"
  [filepath]
  (->>
   filepath
   slurp-lines
   (map #(re-find #"<true>" %))
   (keep-indexed #(if %2 (inc %1)))))

(deftest test-post-*crement-atom?
  (testing "post-*crement? finds all atoms in c file"
    (let [filepath   (resource-path "post-increment.c")
          expected   (true-lines filepath)
          lines  (->> filepath
                      tu
                      post-*crement-atoms
                      (map loc)
                      (map :line))]

     (is (= expected lines))
    )))

(defn post-*crement-atom? [node]
  ))

(remove #{2} [1 2 3])

(def tree (->>
            "post-increment.c"
            resource-path
            tu
            ))
      (def f1 (get-in-tree [0 2 1] tree)) ; false
      (def t1 (get-in-tree [0 2 2 0] tree)) ; true
      (def t2 (get-in-tree [0 2 3 1] tree)) ; true
      (def t2 (get-in-tree [0 2 4] tree))   ; true
      (def f2 (get-in-tree [0 2 5 2] tree)) ; false
      (def t3 (get-in-tree [0 2 6] tree)) ; true

  (write-ast t1)
  (post-*crement-atom? t1)

  (post-*crement-atom? t2)

  (post-*crement-atom? f2)
  (write-ast t2)
  (map write-ast (children t2))

  (write-ast f1)
  (typename f1)

 )

(->>
 "post-increment.c"
 resource-path
 tu
 ;(get-in-tree [0 2 3]) ; false
 (get-in-tree [0 2 4]) ; false
 children
 (map typename)
 )

; if the parent of the node is ExpressionStatement it's value isn't used?