(ns unmo.core-test
  (:require [clojure.test :refer :all]
            [unmo.core :refer :all]))

(deftest format-response-test
  (testing "format-responseは"
    (let [format-response #'unmo.core/format-response]
      (testing "Responder からの結果を整形して返す"
        (are [res] (= "What> テスト" (format-response res))
          {:responder :what :response "テスト"}))

      (testing "WhatResponder の結果を文字列にして返す"
        (is (= "What> テストってなに？"
               (-> {:responder :what :input "テスト"}
                   (unmo.responder/response)
                   (format-response)))))

      (testing "エラーが発生した場合"
        (testing ":responseの値を警告にして返す"
          (let [param   {:responder :random :dictionary {}}
                res     (unmo.responder/response param)
                message (get-in res [:error :message])]
            (is (= (str "Random> 警告: " message)
                   (format-response res)))))))))
