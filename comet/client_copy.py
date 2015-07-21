#!/usr/bin/python
from gevent import monkey; monkey.patch_all()
import gevent
import socket
import time
import  datetime
import  json
import thread


def heartbeat(interval, sk, client_id):
    while(1):
        gevent.sleep(interval)
        print "begin heart beat: %s, client_id: %s" % (datetime.datetime.now(), client_id)
        sk.send("h")

address = ('120.26.112.189', 6969)


def client(client_id):

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    while True:
        try:
            s.connect(address)
            break
        except Exception, e:
            gevent.sleep(0.1)
            continue


    if 1 == 1:
        uid="uid%d"%(datetime.datetime.now().microsecond)
        print uid
        cd={}
        cd["is_token"]=1
        cd["is_web_socket"]=0
        cd["user_token"]="testtoken"
        cd["mac_token"]="mactoken"
        cd["mac"]="macaddress"
        cd["userid"]=str(client_id)
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
        g = gevent.spawn(heartbeat, cd["heart"]-5, s, client_id)
        s.send("h")
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
                        print "heart beat time: %s, client_id: %s" % (datetime.datetime.now(), client_id)


                    elif realdata[0]=="$":
                        lenth=int(realdata[1:])
                        print "len:",lenth,datetime.datetime.now()
                        data=s.recv(lenth+2)
                        print "received message: %s, client_id: %s" % (data,datetime.datetime.now(), client_id)
                    received=received[index+2:]
                    print "left data: %s, client_id: %s" % (received, client_id)
                else:
                    continue
            else:
                break


    s.close()

if __name__ == "__main__":
    gs = []
    for i in range(2):
        gs.append(gevent.spawn(client, i))
    for g in gs:
        g.join()