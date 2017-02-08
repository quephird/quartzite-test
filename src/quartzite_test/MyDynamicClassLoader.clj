(ns quartzite-test.MyDynamicClassLoader
  (:gen-class
    :extends clojure.lang.DynamicClassLoader
    :implements [org.quartz.spi.ClassLoadHelper]))

(defn -initialize [this]
  nil)

(defn -loadClass
  [this name clazz]
    (.loadClass this true))

(defn -getClassLoader [this]
  this)

(defn -getResource [name]
  nil)

(defn -getResourceAsStream [name]
  nil)
