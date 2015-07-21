#!/usr/bin/python
import socket
import time
import  datetime
import  json
import thread


def heartbeat(interval,sk):
    while(1):
        time.sleep(interval)
        print "begin heart beat:",datetime.datetime.now()
        sk.send("+h\r\n")

address = ('120.26.112.189', 6969)
#address = ('127.0.0.1', 6969)
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
while True:
    try:
        s.connect(address)
        break
    except Exception, e:
        time.sleep(0.1)
        continue


if 1==1:
    uid="uid%d"%(datetime.datetime.now().microsecond)
    print uid
    cd={}
    cd["is_token"]=1
    cd["is_web_socket"]=0
    cd["user_token"]="4133a5483163db67f343a247eb39cbdffcb9b20cbe55ed63607639296664420797d688a94149a3ebc0e1170d1c24a90cfdc7649f907ecb25f3b974c32698ef85b6b45d18f91a06f71149713ded8e2335120fa23faaa9de2b9cbd51bbeb0d9a2dee75134df5306c923863cdb7839bfce9b74d13a55ba8849e13ec23ff163e9344"
    cd["mac_token"]="mactoken"
    cd["mac"]="macaddress"
    cd["userid"]="1534636625295881710"
    cd["appplt"]="python"
    cd["appversion"]="1.0"
    cd["protoversion"]="1.0"
    cd["heart"]=31
    st=json.dumps(cd)
    st="$%d\r\n%s\r\n"%(len(st),st)
    print st
    s.send(st)
    received=""
    connect_established=False
    thread.start_new_thread(heartbeat, (cd["heart"]-5,s))
    s.send("+h\r\n")
    while True:
        data = s.recv(1)
        if len(data)>0:
        # if(data != 'start'):
        #     continue;
            #print 'the data received is',data
        # s.send('hihi')
            received+=data
            #print "received:",received
            index=received.find("\r\n")

            if (index>=0):
                realdata=received[0:index]
                if realdata=="+h":
                    print "heart beat time:",datetime.datetime.now()


                elif realdata[0]=="$":
                    lenth=int(realdata[1:])
                    print "len:",lenth,datetime.datetime.now()
                    data=s.recv(lenth+2)
                    print "received message:"+data,datetime.datetime.now()
                received=received[index+2:]
                print "left data:",received
            else:
                continue
        else:
            break


s.close()
