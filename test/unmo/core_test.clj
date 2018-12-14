(ns unmo.core-test
  (:require [clojure.test :refer :all]
            [unmo.core :refer :all]))

(deftest format-response-test
  (testing "format-responseは"
    (let [format-response #'unmo.core/format-response]
      (testing "Responder からの結果を整形して返す"
        (are [res] (= "What> テスト" (format-response res))
          {:responder :what :response "テスト"})))))
