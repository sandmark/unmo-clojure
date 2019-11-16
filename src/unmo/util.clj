(ns unmo.util
  (:require [clojure.java.io :as io]))

(defn conj-unique [coll x]
  (if (some #{x} coll)
    coll
    (conj coll x)))

(defn file-exists? [filename]
  (.exists (io/as-file filename)))
