(ns unmo.responder-test
  (:require [clojure.test :refer :all]
            [unmo.responder :refer :all]))

(deftest pattern-responder-test
  (testing "PatternResponderは"
    (testing "マッチするパターンを見つけ、"
      (let [dictionary {:pattern {"チョコ(レート)?" ["%match%おいしいよね！"]
                                  "天気"          ["明日晴れるといいなー"]}}
            base-param {:responder :pattern :dictionary dictionary}]
        (testing "候補の中からランダムなものを返す"
          (let [param (assoc base-param :input "今日は天気がいい")
                res   (response param)
                text  (:response res)]
            (is (= "明日晴れるといいなー" text))))

        (testing "%match%を正規表現でマッチしたパターンに置き換えて返す"
          (is (= "チョコおいしいよね！"
                 (-> base-param (assoc :input "チョコ食べたい") (response) (:response))))
          (is (= "チョコレートおいしいよね！"
                 (-> base-param (assoc :input "チョコレート食べたい") (response) (:response)))))))

    (testing "マッチするパターンがなかった場合"
      (let [param {:input "nothing" :dictionary {} :responder :pattern}]
        (testing ":errorを設定して返す"
          (is (contains? (response param) :error)))

        (testing ":error :messageに詳細メッセージを設定する"
          (is (clojure.string/includes? "パターンがありません"
                                        (get-in (response param) [:error :message]))))

        (testing ":error :typeに:no-matchを設定する"
          (is (= :no-match (get-in (response param) [:error :type]))))))))

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
