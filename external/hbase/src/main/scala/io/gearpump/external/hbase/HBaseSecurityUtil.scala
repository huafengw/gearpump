package io.gearpump.external.hbase

import java.net.InetAddress

import io.gearpump.cluster.UserConfig
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.security.UserProvider
import org.apache.hadoop.security.UserGroupInformation

object HBaseSecurityUtil {
  val KEYTAB_FILE_KEY = "gearpump.keytab.file"
  val PRINCIPAL_KEY = "gearpump.kerberos.principal"

  def login(userconfig: UserConfig, hbaseConfig: Configuration): UserProvider = {
    val provider = UserProvider.instantiate(hbaseConfig)
    if(UserGroupInformation.isSecurityEnabled){
      val principal = userconfig.getString(PRINCIPAL_KEY).get
      val keytab = userconfig.getString(KEYTAB_FILE_KEY).get
      hbaseConfig.set(KEYTAB_FILE_KEY, keytab)
      hbaseConfig.set(PRINCIPAL_KEY, principal)
      provider.login(KEYTAB_FILE_KEY, PRINCIPAL_KEY, InetAddress.getLocalHost.getCanonicalHostName)
    }
    provider
  }
}
