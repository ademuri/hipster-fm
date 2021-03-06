dataSource {
    pooled = true
    driverClassName = "com.mysql.jdbc.Driver"
	dialect = "org.hibernate.dialect.MySQL5InnoDBDialect"
    username = "hipster"
    password = "Neutral Milk Hotel"
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
}
// environment specific settings
environments {
    development {
        dataSource {
            dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
            url = "jdbc:mysql://localhost/hipster_dev"
			properties {
				defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_UNCOMMITTED
			}
        }
    }
    test {
        dataSource {
            dbCreate = "update"
            url = "jdbc:mysql://localhost/hipster_dev"
			properties {
				defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_UNCOMMITTED
			}
        }
    }
    production {
        dataSource {
			username = ""
			password = ""
            dbCreate = "update"
            url = "jdbc:mysql://127.0.0.1/adam_hipster?zeroDateTimeBehavior=convertToNull"
            properties {
               maxActive = -1
               minEvictableIdleTimeMillis=1000 * 60 * 30
			   timeBetweenEvictionRunsMillis=1000 * 60 * 30
               numTestsPerEvictionRun=3
               testOnBorrow=true
               testWhileIdle=true
               testOnReturn=true
               validationQuery="SELECT 1"
			   
			   // trying to fix idle connection problems
			   maxWait = 1800000
			   maxIdle = 10
			   minIdle = 0
			   
			   // concurrency not getting fresh data problems
			   defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_UNCOMMITTED
            }
        }
    }
	stage {
		dataSource {
			username = ""
			password = ""
			dbCreate = "update"
			url = "jdbc:mysql://127.0.0.1/adam_hipster_stage?zeroDateTimeBehavior=convertToNull"
			properties {
			   maxActive = -1
			   minEvictableIdleTimeMillis=1000 * 60 * 15
			   timeBetweenEvictionRunsMillis=1000 * 60 * 15
			   numTestsPerEvictionRun=3
			   testOnBorrow=true
			   testWhileIdle=true
			   testOnReturn=true
			   validationQuery="SELECT 1"
			   
			   // trying to fix idle connection problems
			   maxWait = 10000
			   maxIdle = 10
			   minIdle = 0
			}
		}
	}
}
