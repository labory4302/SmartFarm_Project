#include <mysql_connection.h>
#include <cppconn/driver.h>
#include <cppconn/exception.h>
#include <cppconn/resultset.h>
#include <cppconn/statement.h>
#include <iostream>

using namespace std;


		class SqlConnect
		{
				sql::Driver *driver;
				sql::Connection *conn;
				sql::Statement *stmt;
				sql::ResultSet *res;
				string insertCmd="INSERT INTO ";
				string ID = "1";
				string recvTime = "now()";
				public:
					SqlConnect(string domain,string user, string passwd);
					SqlConnect(string domain,string user, string passwd,string dbName);
					~SqlConnect();
					void useDatabase(string database);
					void insertSql(string tbName,string values);
					void insertRaspiData(string tbName, string humi, string temp);
					void updateSaveRaspiData(string tbName, string humi);
					void updateArduinoStatus(string tbName, string automode, string pump, string fan, string led);

		};

