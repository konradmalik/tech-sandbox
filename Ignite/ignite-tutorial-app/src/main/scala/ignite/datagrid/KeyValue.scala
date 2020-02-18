package ignite.datagrid

import org.apache.ignite.{Ignite, IgniteCache, Ignition}

object KeyValue {

  // BELOW CODE IS EXECUTED ON THE CLUSTER, SO ALL PRINTS ARE ON THE NODE THAT RUNS THE CODE! (may not see those prints locally)
  // try to run as server (setClientMode(false)) to see some of print
  def main(args: Array[String]): Unit = {

    // Connecting to the cluster (explicitly set client mode)
    Ignition.setClientMode(true)
    val ignite: Ignite = Ignition.start("example.xml")

    val cache: IgniteCache[Int, String] = ignite.getOrCreateCache("myCacheName")

    // Store keys in cache (values will end up on different cache nodes).
    Range(0, 10).foreach(i => cache.put(i, i.toString))

    Range(0, 10).foreach(i => println(cache.get(i)))

    // clear data
    cache.destroy()
    // always close
    cache.close()

    ignite.close()

  }

}
