(ns unmo.responder)

(defmulti response :responder)

(defmethod response :what [params]
  (str (:input params) "ってなに？"))

(defmethod response :default [params]
  (throw (IllegalArgumentException.
          (str "Responder " (params :responder) " が存在しません。"))))
