// Copyright © 2014 Terry Mao, LiuDing All rights reserved.
// This file is part of gopush-cluster.

// gopush-cluster is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// gopush-cluster is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with gopush-cluster.  If not, see <http://www.gnu.org/licenses/>.

package main

import (
	"bufio"
	log "github.com/alecthomas/log4go"
	"errors"
	"io"
	"net"
	"strconv"
	"time"
	"encoding/json"
	"./controller"
)

const (
	minCmdNum = 1
	maxCmdNum = 5
)

var (
	// cmd parse failed
	ErrProtocol = errors.New("cmd format error")
)

// tcpBuf cache.
type tcpBufCache struct {
	instance []chan *bufio.Reader
	round    int
}

// newTCPBufCache return a new tcpBuf cache.
func newtcpBufCache() *tcpBufCache {
	inst := make([]chan *bufio.Reader, 0, Conf.BufioInstance)
	log.Debug("create %d read buffer instance", Conf.BufioInstance)
	for i := 0; i < Conf.BufioInstance; i++ {
		inst = append(inst, make(chan *bufio.Reader, Conf.BufioNum))
	}
	return &tcpBufCache{instance: inst, round: 0}
}

// Get return a chan bufio.Reader (round-robin).
func (b *tcpBufCache) Get() chan *bufio.Reader {
	rc := b.instance[b.round]
	// split requets to diff buffer chan
	if b.round++; b.round == Conf.BufioInstance {
		b.round = 0
	}
	return rc
}

// newBufioReader get a Reader by chan, if chan empty new a Reader.
func newBufioReader(c chan *bufio.Reader, r io.Reader) *bufio.Reader {
	select {
	case p := <-c:
		p.Reset(r)
		return p
	default:
		log.Warn("tcp bufioReader cache empty")
		return bufio.NewReaderSize(r, Conf.RcvbufSize)
	}
}

// putBufioReader pub back a Reader to chan, if chan full discard it.
func putBufioReader(c chan *bufio.Reader, r *bufio.Reader) {
	r.Reset(nil)
	select {
	case c <- r:
	default:
		log.Warn("tcp bufioReader cache full")
	}
}

// StartTCP Start tcp listen.
func StartTCP() error {
	for _, bind := range Conf.TCPBind {
		log.Info("start tcp listen addr:\"%s\"", bind)
		go tcpListen(bind)
	}

	return nil
}

func tcpListen(bind string) {
	addr, err := net.ResolveTCPAddr("tcp", bind)
	if err != nil {
		log.Error("net.ResolveTCPAddr(\"tcp\"), %s) error(%v)", bind, err)
		panic(err)
	}
	l, err := net.ListenTCP("tcp", addr)
	if err != nil {
		log.Error("net.ListenTCP(\"tcp4\", \"%s\") error(%v)", bind, err)
		panic(err)
	}
	// free the listener resource
	defer func() {
		log.Info("tcp addr: \"%s\" close", bind)
		if err := l.Close(); err != nil {
			log.Error("listener.Close() error(%v)", err)
		}
	}()
	// init reader buffer instance
	rb := newtcpBufCache()
	for {
		log.Trace("start accept")
		conn, err := l.AcceptTCP()
		if err != nil {
			log.Error("listener.AcceptTCP() error(%v)", err)
			continue
		}
		if err = conn.SetKeepAlive(Conf.TCPKeepalive); err != nil {
			log.Error("conn.SetKeepAlive() error(%v)", err)
			conn.Close()
			continue
		}
		if err = conn.SetReadBuffer(Conf.RcvbufSize); err != nil {
			log.Error("conn.SetReadBuffer(%d) error(%v)", Conf.RcvbufSize, err)
			conn.Close()
			continue
		}
		if err = conn.SetWriteBuffer(Conf.SndbufSize); err != nil {
			log.Error("conn.SetWriteBuffer(%d) error(%v)", Conf.SndbufSize, err)
			conn.Close()
			continue
		}
		// first packet must sent by client in specified seconds
		if err = conn.SetReadDeadline(time.Now().Add(fitstPacketTimedoutSec)); err != nil {
			log.Error("conn.SetReadDeadLine() error(%v)", err)
			conn.Close()
			continue
		}
		rc := rb.Get()
		// one connection one routine
		go handleTCPConn(conn, rc)
		log.Trace("accept finished")
	}
}

// hanleTCPConn handle a long live tcp connection.
func handleTCPConn(conn net.Conn, rc chan *bufio.Reader) {
	addr := conn.RemoteAddr().String()
	log.Debug("<%s> handleTcpConn routine start", addr)
	rd := newBufioReader(rc, conn)
	if data, err := parseClientData(rd); err == nil {
		log.Info("received connect data:%s, ip:%s",string(data), addr)
		// return buffer bufio.Reader
		connectionInfo:=&ConnectionData{}
		err:=json.Unmarshal(data,&connectionInfo)
		if (err!=nil){
			log.Error("<%s> unmarchal connection data error(%v)", err)
			msg,_:=json.Marshal(&ErrorMessageData{Er:422, Desc:"ConnectionData unmarchal error", Ext:""})
			conn.Write(SimpleMsgConvert("-", string(msg)))
			conn.Close()
			return
		}

		//auth connetion
		thriftClient, err := thriftPool.Get()
		if (err != nil) {
			log.Error("cannot get thrift client from pool, error(%v)", err)
			thriftClient, err = thriftPool.Get()
			if (err != nil) {
				log.Error("cannot get thrift client from pool again, error(%v)", err)
				msg,_:=json.Marshal(&ErrorMessageData{Er:500, Desc:"thrift pool error", Ext: ""})
				sendmsg := SimpleMsgConvert("-", string(msg))
				log.Error("user:%s tk:%s, auth rpc error(%v), send:%s", connectionInfo.Userid,
					connectionInfo.User_token, err, string(sendmsg))
				conn.Write(sendmsg)
				conn.Close()
				return
			}
		}

		authres, err:= thriftClient.(*controller.ControllerRpcServiceClient).Auth(connectionInfo.Userid, connectionInfo.User_token)
		thriftPool.Back(thriftClient)

		if (err!=nil){

			msg,_:=json.Marshal(&ErrorMessageData{Er:500, Desc:"auth server error", Ext: ""})
			sendmsg := SimpleMsgConvert("-", string(msg))
			log.Error("user:%s tk:%s, auth rpc error(%v), send:%s", connectionInfo.Userid,
				connectionInfo.User_token, err, string(sendmsg))
			conn.Write(sendmsg)
			conn.Close()
			return
		}

		if authres!=0{//验证失败
			msg,_:=json.Marshal(&ErrorMessageData{Er:int(authres),Desc:"auth error",Ext:""})
			sendmsg := SimpleMsgConvert("-", string(msg))
			log.Warn("user:%s tk:%s, auth code(%s), send:%s", connectionInfo.Userid, connectionInfo.User_token,
				msg, string(sendmsg))
			conn.Write(sendmsg)
			conn.Close()
			return
		}

		SubscribeTCPHandle(conn, connectionInfo)

	} else {
		// return buffer bufio.Reader
		putBufioReader(rc, rd)
		log.Error("<%s> parseCmd() error(%v)", addr, err)
	}
	// close the connection
	if err := conn.Close(); err != nil {
		log.Error("<%s> conn.Close() error(%v)", addr, err)
	}
	log.Debug("<%s> handleTcpConn routine stop", addr)
	return
}

// SubscribeTCPHandle handle the subscribers's connection.
func SubscribeTCPHandle(conn net.Conn, cd *ConnectionData) {
	addr := conn.RemoteAddr().String()
	if cd.Heart < minHearbeatSec {
		conn.Write(SimpleMsgConvert("-","heart beat must more than " + string(minHearbeatSec)))
		log.Warn("<%s> user_key:\"%s\" heartbeat argument error, less than %d", conn.RemoteAddr().String(),cd.Userid , minHearbeatSec)
		return
	}
	heartbeat := cd.Heart + delayHeartbeatSec

	// fetch subscriber from the channel
	c, err := UserChannel.Get(cd.Userid, true)
	if err != nil {
		log.Warn("<%s> user_key:\"%s\" can't get a channel (%s)", conn.RemoteAddr().String(), cd.Userid, err)
		conn.Write(SimpleMsgConvert("-","can't get a channel"))
		return
	}
	// auth token
//	if ok := c.AuthToken(cd.userid, cd.user_token); !ok {
//		conn.Write(AuthReply)
//		log.Error("<%s> user_key:\"%s\" auth token \"%s\" failed", addr, key, token)
//		return
//	}
	// add a conn to the channel
	connElem, err := c.AddConn(cd.Userid, &Connection{Conn: conn, Proto: TCPProto, Version: cd.Protoversion})
	if err != nil {
		log.Error("<%s> user_key:\"%s\" add conn error(%v)", conn.RemoteAddr().String(), cd.Userid, err)
		return
	}
	// blocking wait client heartbeat
	reply := []byte{0,0,0,0}
	// reply := make([]byte, HeartbeatLen)
	last_sync_time:=time.Now().UnixNano()
	begin := time.Now().UnixNano()
	end := begin + Second

	flag := 1
	for flag == 1{
		select {
			case res := <- connElem.Value.(*Connection).AppLogicBuf:{//这里有个问题：当applogicBuf有2消息过来后，default那边有conn.Read会阻塞，导致那边必须读到东西，才能开始新的循环。
				//如果不阻塞read，又会导致for循环不断的执行，浪费cpu。
				if(res==2){
					log.Info("direct logout user %s, connip:%s", cd.Userid, conn.RemoteAddr().String())
					//connElem.Value.(*Connection).WriteNotify(cd.Userid, DirectOfflineNotifyStr)
					flag = 0
				}
			}

			default :{
				// more then 1 sec, reset the timer
				if end-begin >= Second {
					if err = conn.SetReadDeadline(time.Now().Add(time.Second * time.Duration(heartbeat))); err != nil {
						log.Error("<%s> user_key:%s conn.SetReadDeadLine() error(%v)",conn.RemoteAddr().String(), cd.Userid, err)
						flag = 0
						continue;
					}
					begin = end
				}

				if _, err = conn.Read(reply); err != nil {

					if err != io.EOF {
						log.Info("<%s> user_key:\"%s\" conn.Read() failed, read heartbeat timedout error(%v)", conn.RemoteAddr().String(), cd.Userid, err)
					} else {
						// client connection close
						log.Info("<%s> user_key:\"%s\" client connection close error(%v)", conn.RemoteAddr().String(), cd.Userid, err)
					}

					flag = 0
					continue;
				}

				log.Debug("receive from userid:%s, ip:%s, Received:%s", cd.Userid, addr, string(reply))
				if string(reply) == Heartbeat {
					log.Debug("receive hearbeat from userid:%s, ip:%s, Received:%s", cd.Userid, addr, string(reply))
					connElem.Value.(*Connection).WriteComand(cd.Userid, "h")//必须derect，不然可能先处理另外一个case导致connect被断开，这里会出现channel close异常。
//					if _, err = conn.Write(SimpleMsgConvert("+","h")); err != nil {
//						flag = 0
//						continue;
//						log.Warn("res heartbeat erro to userid:%s, ip:%s", cd.Userid, addr)
//					}
				} else {//暂时前端只有heartbeat请求
					flag = 0
					log.Warn("receive unknown heartbeat protocal from userid:%s, ip:%s, Received:%s", cd.Userid, addr, string(reply))
				}

				//和服务器端sync状态.在线通知
				end = time.Now().UnixNano()
				if end-last_sync_time>=Second*Conf.ControllerSyncInterval{
					last_sync_time=end
					log.Debug("notify user %s online status:1, ip:%s, node:%s", cd.Userid, addr, Conf.NodeName)
					thriftClient, err := thriftPool.Get()
					if (err != nil) {
						log.Error("cannot get thrift client from pool, error(%v)", err)
					}

					thriftClient.(*controller.ControllerRpcServiceClient).NotifyConnectionStatus(Conf.NodeName,cd.Userid,1)
					thriftPool.Back(thriftClient)
				}
			}

		}
	}

//	for {
//		// more then 1 sec, reset the timer
//		if end-begin >= Second {
//			if err = conn.SetReadDeadline(time.Now().Add(time.Second * time.Duration(heartbeat))); err != nil {
//				log.Error("<%s> user_key:\"%s\" conn.SetReadDeadLine() error(%v)",conn.RemoteAddr().String(), cd.Userid, err)
//				break
//			}
//			begin = end
//		}
//		//todo  如果需要接受前端发来的内容，需要做额外处理，这儿。
//		if _, err = conn.Read(reply); err != nil {
//			if err != io.EOF {
//				log.Warn("<%s> user_key:\"%s\" conn.Read() failed, read heartbeat timedout error(%v)", conn.RemoteAddr().String(), cd.Userid, err)
//			} else {
//				// client connection close
//				log.Warn("<%s> user_key:\"%s\" client connection close error(%v)", conn.RemoteAddr().String(), cd.Userid, err)
//			}
//			break
//		}
//		log.Debug("receive from userid:%s, ip:%s, Received:%s", cd.Userid, addr, string(reply))
//		if string(reply) == Heartbeat {
//			if _, err = conn.Write(SimpleMsgConvert("+","h")); err != nil {
//				log.Error("reponse heartbeat to userid:%s error, ip:%s, error:(%v)", cd.Userid, addr, err)
//				break
//			}
//			log.Debug("receive heartbeat from userid:%s, ip:%s, Received:%s", cd.Userid, addr, string(reply))
//		} else {//暂时前端只有heartbeat请求
//			log.Warn("receive unknown heartbeat protocal from userid:%s, ip:%s, Received:%s", cd.Userid, addr, string(reply))
//			break
//		}
//
//		//和服务器端sync状态.在线通知
//		end = time.Now().UnixNano()
//		if end-last_sync_time>=Second*Conf.ControllerSyncInterval{
//			last_sync_time=end
//			controllerClient.NotifyConnectionStatus(Conf.NodeName,cd.Userid,1)
//		}
//	}

	// remove exists conn
	if err := c.RemoveConn(cd.Userid, connElem); err != nil {
		log.Error("receive unknown heartbeat protocal from userid:%s, ip:%s, Received:%s", cd.Userid, addr, string(reply))
		log.Error("<%s> user_key:\"%s\" remove conn error(%v)", conn.RemoteAddr().String(), cd.Userid, err)
	}
	return
}
func parseClientData(rd *bufio.Reader)([]byte,error){
	//获取ConnectionData并且解析，验证内容.
	dataLen,err := parseCmdSize(rd,'$')
	if err != nil {
		log.Error("tcp:connection data format error when find '$' (%v)", err)
		return nil, err
	}
	// get argument data
	d, err := parseCmdData(rd, dataLen)
	if err != nil {
		log.Error("tcp:parseCmdData() error(%v)", err)
		return nil, err
	}
	return d,err
}

// parseCmd parse the tcp request command.
func parseCmd(rd *bufio.Reader) ([]string, error) {
	// get argument number
	argNum, err := parseCmdSize(rd, '*')
	if err != nil {
		log.Error("tcp:cmd format error when find '*' (%v)", err)
		return nil, err
	}
	if argNum < minCmdNum || argNum > maxCmdNum {
		log.Error("tcp:cmd argument number length error")
		return nil, ErrProtocol
	}
	args := make([]string, 0, argNum)
	for i := 0; i < argNum; i++ {
		// get argument length
		cmdLen, err := parseCmdSize(rd, '$')
		if err != nil {
			log.Error("tcp:parseCmdSize(rd, '$') error(%v)", err)
			return nil, err
		}
		// get argument data
		d, err := parseCmdData(rd, cmdLen)
		if err != nil {
			log.Error("tcp:parseCmdData() error(%v)", err)
			return nil, err
		}
		// append args
		args = append(args, string(d))
	}
	return args, nil
}

// parseCmdSize get the request protocol cmd size.
func parseCmdSize(rd *bufio.Reader, prefix uint8) (int, error) {
	// get command size
	cs, err := rd.ReadBytes('\n')
	if err != nil {
		log.Debug("received:%d %s",rd.Buffered(),rd)
		log.Error("tcp:rd.ReadBytes('\\n') error(%v)", err)
		return 0, err
	}
	csl := len(cs)
	if csl <= 3 || cs[0] != prefix || cs[csl-2] != '\r' {
		log.Error("tcp:\"%v\"(%d) number format error, length error or prefix error or no \\r", cs, csl)
		return 0, ErrProtocol
	}
	// skip the \r\n
	cmdSize, err := strconv.Atoi(string(cs[1 : csl-2]))
	if err != nil {
		log.Error("tcp:\"%v\" number parse int error(%v)", cs, err)
		return 0, ErrProtocol
	}
	return cmdSize, nil
}

// parseCmdData get the sub request protocol cmd data not included \r\n.
func parseCmdData(rd *bufio.Reader, cmdLen int) ([]byte, error) {
	d, err := rd.ReadBytes('\n')
	if err != nil {
		log.Error("tcp:rd.ReadBytes('\\n') error(%v)", err)
		return nil, err
	}
	dl := len(d)
	// check last \r\n
	if dl != cmdLen+2 || d[dl-2] != '\r' {
		log.Error("tcp:\"%v\"(%d) number format error, length error or no \\r", d, dl)
		return nil, ErrProtocol
	}
	// skip last \r\n
	return d[0:cmdLen], nil
}
