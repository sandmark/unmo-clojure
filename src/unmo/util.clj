(ns unmo.util
  (:require [clojure.java.io :as io]))

(defn file-exists? [filename]
  (.exists (io/as-file filename)))
