(ns unmo.util)

(defn conj-unique
  "コレクションcollに要素xがない場合のみconjを適用する。"
  [coll x]
  (if (some #{x} coll)
    coll
    (conj coll x)))
