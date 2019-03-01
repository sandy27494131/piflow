package cn.piflow.bundle.streaming

import cn.piflow.{JobContext, JobInputStream, JobOutputStream, ProcessContext}
import cn.piflow.conf._
import cn.piflow.conf.bean.PropertyDescriptor
import cn.piflow.conf.util.{ImageUtil, MapUtil}
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.{DStream, InputDStream, ReceiverInputDStream, SocketReceiver}
import org.apache.spark.streaming.{Seconds, StreamingContext}

class SocketTextStream extends ConfigurableStreamingStop {
  override val authorEmail: String = "xjzhu@cnic.cn"
  override val description: String = "Receive text data from a data server listening on a TCP socket."
  override val inportList: List[String] = List(PortEnum.NonePort)
  override val outportList: List[String] = List(PortEnum.DefaultPort)
  override val timing: Integer = 1

  var hostname:String =_
  var port:String=_
  var schema:String=_

  override def setProperties(map: Map[String, Any]): Unit = {
    hostname=MapUtil.get(map,key="hostname").asInstanceOf[String]
    port=MapUtil.get(map,key="port").asInstanceOf[String]
    schema=MapUtil.get(map,key="schema").asInstanceOf[String]
  }

  override def getPropertyDescriptor(): List[PropertyDescriptor] = {
    var descriptor : List[PropertyDescriptor] = List()
    val hostname = new PropertyDescriptor().name("hostname").displayName("hostname").description("socket hostname ").defaultValue("").required(true)
    val port = new PropertyDescriptor().name("port").displayName("port").description("socket port").defaultValue("").required(true)
    val schema = new PropertyDescriptor().name("schema").displayName("schema").description("data schema").defaultValue("").required(true)
    descriptor = hostname :: descriptor
    descriptor = port :: descriptor
    descriptor = schema :: descriptor
    descriptor
  }

  //TODO: change icon
  override def getIcon(): Array[Byte] = {
    ImageUtil.getImage("icon/streaming/socketTextStream.png")
  }

  override def getGroup(): List[String] = {
    List(StopGroup.StreamingGroup)
  }

  override def initialize(ctx: ProcessContext): Unit = {

  }

  override def perform(in: JobInputStream, out: JobOutputStream, pec: JobContext): Unit = {

    val spark = pec.get[SparkSession]();
    val socketDF = spark
      .readStream
      .format("socket")
      .option("host",hostname)
      .option("port",port)
      .load()

    out.write(socketDF)
  }



  /*override def getDStream[T](ssc : StreamingContext):DStream[T] = {

      //read data
      //val spark = pec.get[SparkSession]();
      //val ssc = new StreamingContext(spark.sparkContext,Seconds(1))
      ssc.socketTextStream(hostname,Integer.parseInt(port))

      //lines.asInstanceOf[ReceiverInputDStream[String]]
  }*/
  override def getDStream(ssc: StreamingContext): DStream[String] = {
    val dstream = ssc.socketTextStream(hostname,Integer.parseInt(port))
    dstream.asInstanceOf[DStream[String]]
  }

}
