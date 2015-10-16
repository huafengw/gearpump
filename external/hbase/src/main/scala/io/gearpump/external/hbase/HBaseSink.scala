/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gearpump.external.hbase

import java.io.{IOException, ObjectInputStream, ObjectOutputStream}
import java.security.PrivilegedExceptionAction

import io.gearpump.cluster.UserConfig
import io.gearpump.streaming.dsl.TypedDataSink
import io.gearpump.streaming.sink.DataSink
import io.gearpump.streaming.task.TaskContext
import io.gearpump.Message
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.{TableName, HBaseConfiguration}
import org.apache.hadoop.hbase.client.{HTable, ConnectionFactory, Put}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.client.Table
import org.apache.hadoop.security.UserGroupInformation

class HBaseSink(userconfig: UserConfig, tableName: String) extends DataSink{
  @transient lazy val configuration = HBaseConfiguration.create()

  lazy val (connection, table) = {
    val principal = userconfig.getString(HBaseSecurityUtil.PRINCIPAL_KEY).get
    val keytab = userconfig.getString(HBaseSecurityUtil.KEYTAB_FILE_KEY).get
    UserGroupInformation.setConfiguration(configuration)
    UserGroupInformation.loginUserFromKeytab(principal, keytab)
    val conn = ConnectionFactory.createConnection(configuration)
    val table = conn.getTable(TableName.valueOf(tableName))
    (conn, table)
  }

  //lazy val table = connection.getTable(TableName.valueOf(tableName))

  override def open(context: TaskContext): Unit = {}

//  def this(tableName: String) = {
//    this(tableName, HBaseConfiguration.create())
//  }

  def insert(put: Put): Unit = {
    table.put(put)
  }

  def insert(rowKey: String, columnGroup: String, columnName: String, value: String): Unit = {
    insert(Bytes.toBytes(rowKey), Bytes.toBytes(columnGroup), Bytes.toBytes(columnName), Bytes.toBytes(value))
  }

  def insert(rowKey: Array[Byte], columnGroup: Array[Byte], columnName: Array[Byte], value: Array[Byte]): Unit = {
    val put = new Put(rowKey)
    put.addColumn(columnGroup, columnName, value)
    insert(put)
  }

  def put(msg: AnyRef): Unit = {
    msg match {
      case seq: Seq[AnyRef] =>
        seq.foreach(put)
      case put: Put =>
        insert(put)
      case tuple: (String, String, String, String) =>
        insert(tuple._1, tuple._2, tuple._3, tuple._4)
      case tuple: (Array[Byte], Array[Byte], Array[Byte], Array[Byte]) =>
        insert(tuple._1, tuple._2, tuple._3, tuple._4)
    }
  }

  override def write(message: Message): Unit = {
    put(message.msg)
  }

  def close(): Unit = {
    connection.close()
    table.close()
  }
}

object HBaseSink {
  val HBASESINK = "hbasesink"
  val TABLE_NAME = "hbase.table.name"
  val COLUMN_FAMILY = "hbase.table.column.family"
  val COLUMN_NAME = "hbase.table.column.name"

  def apply[T](tableName: String): HBaseSink with TypedDataSink[T] = {
    new HBaseSink(UserConfig.empty, tableName) with TypedDataSink[T]
  }

  def apply[T](tableName: String, configuration: Configuration): HBaseSink with TypedDataSink[T] = {
    new HBaseSink(UserConfig.empty, tableName) with TypedDataSink[T]
  }
}
