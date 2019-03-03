package utils

import com.google.common.cache.CacheBuilder
import utils.Global.cfgVevo
import scalacache.ScalaCache
import scalacache.guava.GuavaCache


trait  VevoCache {

  val lruCacheSize: Long
  val lruCacheTtl: Int

  val underlyingGuavaCache =
    CacheBuilder.newBuilder().maximumSize(lruCacheSize).build[String, Object]
  implicit val scalaCache =
    ScalaCache(GuavaCache(underlyingGuavaCache))

}
