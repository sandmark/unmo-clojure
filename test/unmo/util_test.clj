(ns unmo.util-test
  (:require [clojure.test :as t]
            [unmo.util :as util]))

(t/deftest file-exists?-test
  (t/testing "exists"
    (t/is (true? (util/file-exists? "project.clj"))))

  (t/testing "doesn't exists"
    (t/is (false? (util/file-exists? "dummy.clj")))))
