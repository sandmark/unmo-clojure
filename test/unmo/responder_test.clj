(ns unmo.responder-test
  (:require [clojure.test :refer :all]
            [unmo.responder :refer :all]))

(deftest random-responder-test
  (testing "RandomResponderは"
    (testing "ランダム辞書から無作為な値を返す"
      (let [dic {:random ["a" "b" "c"]}
            param {:responder :random :dictionary dic}
            res (response param)]
        (is (some #{(:response res)} (:random dic)))))

    (testing "ランダム辞書が空だった場合、:errorキーを設定して返す"
      (let [dic {:random []}
            param {:responder :random}
            res (response param)]
        (is (contains? res :error))
        (is (clojure.string/includes? (get-in res [:error :message]) "ランダム辞書が空です" ))))))

(deftest what-responder-test
  (testing "WhatResponderは"
    (testing "発言を表現するMapオブジェクトを返す"
      (let [res (response {:responder :what :input "test"})]
        (are [k] (contains? res k)
          :responder
          :input
          :response)))

    (testing "入力 s に対し、常に \"sってなに？\" と返す"
      (let [result {:responder :what :input "テスト" :response "テストってなに？"}]
        (is (= result
               (response {:responder :what :input "テスト"})))))))

(deftest default-responder-test
  (testing "Responderを指定しなかった場合、"
    (testing "例外IllegalArgumentExceptionを投げる"
      (is (thrown-with-msg? IllegalArgumentException #"存在しません"
                            (response {})))))

  (testing "存在しないResponderを指定した場合、"
    (testing "例外IllegalArgumentExceptionを投げる"
      (is (thrown-with-msg? IllegalArgumentException #"存在しません"
                            (response {:responder :not-found}))))))
