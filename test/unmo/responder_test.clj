(ns unmo.responder-test
  (:require [clojure.test :refer :all]
            [unmo.responder :refer :all]))

(deftest what-responder-test
  (testing "WhatResponderは"
    (testing "入力 s に対し、常に \"sってなに？\" と返す"
      (is (= "テストってなに？" (response {:responder :what :input "テスト"}))))))

(deftest default-responder-test
  (testing "Responderを指定しなかった場合、"
    (testing "例外IllegalArgumentExceptionを投げる"
      (is (thrown-with-msg? IllegalArgumentException #"存在しません"
                            (response {})))))

  (testing "存在しないResponderを指定した場合、"
    (testing "例外IllegalArgumentExceptionを投げる"
      (is (thrown-with-msg? IllegalArgumentException #"存在しません"
                            (response {:responder :not-found}))))))
