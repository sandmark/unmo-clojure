(ns unmo.core-test
  (:require [clojure.test :refer :all]
            [clojure.string :refer [includes? starts-with?]]
            [unmo.core :refer :all]
            [unmo.morph :refer [analyze]]))

(deftest dialogue-test
  (testing "dialogueは"
    (let [dialogue #'unmo.core/dialogue]
      (testing "input parts dictionaryを受け取り"
        (let [input "あたしはプログラムの女の子です"
              parts (analyze input)
              dictionary {}]
          (testing "response文字列を返す"
            (let [result (dialogue input parts {:random ["test"]})]
              (is (or (= (str "What> " input "ってなに？"))
                      (= "Random> test" result)))))
          (testing "エラーが発生した場合"
            (testing ":typeが:fatalであれば警告メッセージを表示する"
              (let [result (dialogue input parts dictionary)]
                (is (or (starts-with? result "What")
                        (includes? result "辞書が空")))))
            (testing ":typeが:fatalでなければRandomResponderを呼び出す"
              (let [result (dialogue input parts {:random ["anything"]})]
                (is (starts-with? result "Random>")))))))

      (testing "input parts dictionary responderが指定された場合"
        (let [input "あたしはプログラムの女の子です"
              parts (analyze input)
              dictionary {:pattern {"プログラム" ["あたしが好きなのは%noun%と%noun%です"]}}]
          (testing "指定されたResponderを呼び出す"
            (let [result (dialogue input parts dictionary :pattern)]
              (is (or (= "あたしが好きなのは女の子とプログラムです")
                      (= "あたしが好きなのはプログラムと女の子です"))))))))))

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
