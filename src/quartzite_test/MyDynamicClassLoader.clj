(ns quartzite-test.MyDynamicClassLoader
  (:gen-class
    :extends clojure.lang.DynamicClassLoader
    :implements [org.quartz.spi.ClassLoadHelper]
    :exposes-methods {loadClass parentLoadClass}))

(defn -initialize [this]
  nil)

(defn load-class [loader name]
  "This is named with a dashified format to avoid a StackOverflowError by
   endlessly calling loadClass of _this_ class and not the parent."
  (.parentLoadClass loader name true))

(defn -loadClass
  ([this name]
   (load-class this name))
  ([this name _]
   (load-class this name)))

(defn -getClassLoader [this]
  this)
