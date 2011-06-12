(ns dce.GenericThrowable
  (:gen-class :extends Throwable
              :state state
              :init init
              :constructors {[Object Object Object]
                             []}))

(defn -init
  [obj form env]
  [[] {:obj obj :form form :env env}])

(defn -toString [self]
  (format "#<GenericThrowable dce.GenericThrowable: %s"
          (:obj (.state self))))

(defmacro throw+ [obj]
  `(throw (dce.GenericThrowable. ~obj {} {})))

(defn- catch-form? [x]
  (and (seq? x) (= 'catch (first x))))

(defmacro try+
  [& body]
  (let [catch-clauses (filter catch-form? body)
        try-body (remove catch-form? body)
        throwable (gensym)
        state (gensym)
        stack (gensym)
        thrown (gensym)
        &thrown-form (gensym)
        &thrown-env (gensym)]
    `(try
       ~@try-body
       (catch Throwable ~throwable
         (let [[~thrown ~&thrown-form ~&thrown-env]
               (if (instance? dce.GenericThrowable ~throwable)
                 (let [~state (.state ~throwable)
                       ~stack (drop 3 (.getStackTrace ~throwable))]
                   [(:obj ~state)
                    (vary-meta (:form ~state) assoc :stack ~stack)
                    (:env ~state)])
                 [~throwable])]
           (cond
            ~@(mapcat
               (fn [[_  type local-name & catch-body]]
                 `[(isa? (type ~thrown) ~type)
                   (let [~local-name ~thrown]
                     ~@catch-body)])
               catch-clauses)
            :else (throw ~throwable)))))))

