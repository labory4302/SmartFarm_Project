#include "SqlConnect.h"

SqlConnect::SqlConnect(string domain, string user, string passwd)
{
    try {
        driver=get_driver_instance();
        conn=driver->connect(domain,user,passwd);


    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line "<< __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
    }


}

SqlConnect::SqlConnect(string domain, string user, string passwd, string dbName)
{
    try {
        driver=get_driver_instance();
        conn=driver->connect(domain,user,passwd);
        useDatabase(dbName);

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line "<< __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
    }


}


SqlConnect::~SqlConnect()
{

}

void SqlConnect::useDatabase(string database)
{
    try {
        conn->setSchema(database);
        stmt=conn->createStatement();

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line "<< __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
    }

}

void SqlConnect::insertSql(string tbName, string values)
{
//    cout<<"SQL COMMAND : "<<insertCmd+tbName+" values"+'('+values+");"<<endl;
    try {
        stmt->execute(insertCmd+tbName+" values"+'('+values+");");

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line "<< __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;

        cout <<"ERR CMD : "<<insertCmd+tbName+" values"+'('+values+");"<<endl;
    }

}

void SqlConnect::insertRaspiData(string tbName, string humi, string temp)
{
    try {
        stmt->execute(insertCmd+tbName+" values"+'('+ID+','+recvTime+','+humi+','+temp+')'+';');

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line "<< __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;

    //    cout <<"ERR CMD : "<<insertCmd+tbName+" values"+'('+values+");"<<endl;
    }

}

void SqlConnect::updateSaveRaspiData(string tbName, string humi)
{
    try {
        stmt->execute("UPDATE "+tbName+" SET recvDate = now(), Humi = "+humi+ " WHERE userNo = "+ ID +";");

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line "<< __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;

       // cout <<"ERR CMD : "<<insertCmd+tbName+" values"+'('+values+");"<<endl;
    }

}

void SqlConnect::updateArduinoStatus(string tbName, string automode, string pump, string fan, string led)
{
    try {
        stmt->execute("UPDATE "+tbName+" SET automode = "+automode+", pump = "+pump+", fan = "+fan+", led = "+led+" WHERE userNo = "+ ID +";");
			

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line "<< __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;

       // cout <<"ERR CMD : "<<insertCmd+tbName+" values"+'('+values+");"<<endl;
    }

}

