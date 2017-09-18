(in-ns 'atom-finder.classifier)

(import '(org.eclipse.cdt.core.dom.ast IASTFunctionDeclarator IASTFunctionDefinition))

(defn fn-param-names
  "names of the parameters of the given function"
  [node]
  (when (function-node? node)
    (set (map #(-> % .getDeclarator .getName node-name)
              (->> node .getDeclarator .getParameters)))))

(defn pointer-param?
  "Is this param a pointer?"
  [param]
  (->> param .getDeclarator .getPointerOperators empty? not))

(defn scalar-param-names
  "names of the parameters of the given function"
  [node]
  (when (function-node? node)
    (->> node
         .getDeclarator
         .getParameters
         (remove pointer-param?)
         (map #(-> % .getDeclarator .getName node-name))
         set)))

(->> "int main(int *a, char &b, long c) { }"
     parse-source
     (get-in-tree [0])
     scalar-param-names
     )

(defn repurposed-variable-atom?
  ([node] (repurposed-variable-atom? node (scalar-param-names (enclosing-function node))))
  ([node param-names]
   (and (mutatable-op? node)
        (let [lvalue (first (children node))]
          (or ; argc = 1 OR argv[1] = 1
           (contains? param-names (node-name lvalue))
           (contains? param-names (node-name (first (children lvalue)))))))))

(defn repurposed-variable-atoms
  ([root] (repurposed-variable-atoms root #{}))
  ([root param-names]
   (let [new-param-names (clojure.set/union param-names (scalar-param-names root))]
     (if (repurposed-variable-atom? root param-names)
       [root]
       (mapcat #(repurposed-variable-atoms % new-param-names) (children root))))))

(->> "/Users/dgopstein/opt/src/gcc/gcc/config/avr/avr-devices.c"
     parse-file
     repurposed-variable-atoms
     )
