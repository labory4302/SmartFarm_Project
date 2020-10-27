#include <iostream>
#include "SqlConnect.cpp"
#include <cstdio>
#include <cstring>
#include <cstdlib> 
#include <sys/socket.h>   
#include <signal.h>   
#include <string>
#include <pthread.h>   
#include <unistd.h>   
#include <bluetooth/bluetooth.h>   
#include <bluetooth/rfcomm.h> 
#include <netinet/in.h>
#include <arpa/inet.h>

#define host "dbinstance3.cjytw5i33eqd.us-west-2.rds.amazonaws.com"
#define user "luck0707"
#define passwd "disorder2848"
#define db "example"
#define PORT 9999
#define BUFFER_SIZE 4096
#define BACKLOG 10

using namespace std;
int s ;   
void ctrl_c_handler(int signal);   
void close_sockets();   
void *readMsg(void *);
void *sendArduino(void *);    
void *sendRDS(void *);
int openSocket(int);
void *socketAndroid(void *);

int main(int argc, char** argv)
{   
    (void) signal(SIGINT,ctrl_c_handler);   
    int status(0);  
    pthread_t readT, sendT;   
    char *message1 = "Read thread\n";   
    char *message2 = "Send thread\n";   
    int iret1, iret2;     
    
    openSocket(status);
      
    if(0 == status)
    {    
          cout << "connect success" <<endl;
        // 쓰레드 생성
        iret1 = pthread_create(&readT,NULL,readMsg,(void*) message1);   
        iret2 = pthread_create(&sendT,NULL,socketAndroid,(void*) message2);  
          
        pthread_join(readT,NULL);   
        pthread_join(sendT,NULL);   
      
    }   
      
      
    close_sockets();   
    return 0;   
}   
  
int openSocket(int socketStatus)
{
    struct sockaddr_rc addr= { 0 };     
    char dest[18] = "98:D3:37:90:AF:07";   
    char msg[25];   
     s = socket(AF_BLUETOOTH,SOCK_STREAM,BTPROTO_RFCOMM);   
    addr.rc_family = AF_BLUETOOTH ;   
    addr.rc_channel = 1 ;   
    str2ba(dest,&addr.rc_bdaddr);   
      
    //connect to server   
   // printf("going 2 connect\n");
       cout << "connecting....." <<endl;
    socketStatus = connect(s,(struct sockaddr *)&addr,sizeof(addr)) ; 
    return socketStatus;  
}  
  
void sendSaveDataRDS(char msg[BUFFER_SIZE])
{
        int saveHumi = atoi(msg)%1000;
        SqlConnect sqlConnect(host,user,passwd,db);
        sqlConnect.updateSaveRaspiData("saveRaspiData",to_string(saveHumi));
        cout << "saveHumiData = " << saveHumi << "%" << endl;
      //  printf("saveHumiData = %d%\n",saveHumi);
        
}
  
void sendDataRDS(char* msg[BUFFER_SIZE])
{   
    SqlConnect sqlConnect(host,user,passwd,db);
    sqlConnect.insertRaspiData("RaspiData",msg[0],msg[1]);
    cout << "Humi = " << msg[0] << "  Temp = " << msg[1] << endl;
 //   printf("Humi : %s Temp : %s\n", msg[0], msg[1]);  
}   

void sendStatusRDS(char* msg[BUFFER_SIZE])
{   
    SqlConnect sqlConnect(host,user,passwd,db);
    sqlConnect.updateArduinoStatus("arduinoStatus",msg[1],msg[2],msg[3],msg[4]);
    cout << "autoMode = " << msg[1] << " pemp = " << msg[2] <<  " fan = " << msg[3] << " led = " << msg[4] <<endl;
 //   printf("automode : %s pump : %s fan : %s led : %s\n",msg[1],msg[2],msg[3],msg[4]);  
} 

void sendArduino(char msg[BUFFER_SIZE])
{
    send(s,msg,strlen(msg),0);
 //   printf("sendArduino = %s\n", msg);
    
    if(atoi(msg)/1000==5)
        sendSaveDataRDS(msg);
    else
        cout << "sendArduino = " << msg << endl;
}

void *socketAndroid(void *)
{
    int sockfd, new_fd;  			
    struct sockaddr_in server_addr; 
    struct sockaddr_in client_addr; 
    socklen_t sin_size;
    // ---------------
    char bufRx[BUFFER_SIZE];

    int numbytes;
    // ---------------

    int yes=1;

    if ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
        perror("socket");
        exit(1);
    }

    if (setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int)) == -1) {
        perror("setsockopt");
        exit(1);
    }

    server_addr.sin_family = AF_INET;			
    server_addr.sin_port = htons(PORT);     
    server_addr.sin_addr.s_addr = INADDR_ANY; 
    memset(&(server_addr.sin_zero), '\0', 8); 

    if (bind(sockfd, (struct sockaddr *)&server_addr, sizeof(struct sockaddr)) == -1) {
        perror("bind");
        exit(1);
    }

    if (listen(sockfd, BACKLOG) == -1) {
        perror("listen");
        exit(1);
    }


    while(1) { 

        sin_size = sizeof(struct sockaddr_in);
        if ((new_fd = accept(sockfd, (struct sockaddr *)&client_addr, &sin_size)) == -1) {
            perror("accept");
            continue;
        }

        cout << inet_ntoa(client_addr.sin_addr) << " connect" << endl;
        // recv() ----
         if ((numbytes=recv(new_fd, bufRx, BUFFER_SIZE-1, 0)) == -1) {
             perror("recv");
             exit(1);
         }
        sendArduino(bufRx);
        
    

        close(new_fd);
    }
    close(new_fd);
    return 0;
    
}
  
void *readMsg(void *)
{   
    int bytes_read;  
    char del[] = ",";
    char* token;
    char* passingData[1024];
    char buf[1024];

    do{ 
        int num = 0; 
        
        //아두이노로 부터 전송받은 값 저장   
        memset(buf,0,sizeof(buf));   
        bytes_read = recv(s,buf,sizeof(buf),0) ;   
        token = strtok(buf,del);
        if(bytes_read !=1)
        {
            while(token !=NULL)
            {
                passingData[num] = token;
                token = strtok(NULL,",");
                num++;
            }
            
            if(atoi(passingData[0]) ==1)
                sendStatusRDS(passingData);
            else
                sendDataRDS(passingData);
            
        }
        else
            printf("\n");
        if(bytes_read <= 0)break;   
    
    }while(1);   
}   
 

void close_sockets()
{   
    close(s);   
    fprintf(stdout,"Close sockets\n");   
}   
  
void ctrl_c_handler(int signal)
{      
   
    close_sockets();   
    exit(0);   
}  


